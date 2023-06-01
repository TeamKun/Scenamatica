import json
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QTableWidget, \
    QTableWidgetItem, QHeaderView, QPushButton, QFileDialog, QAction, QMenuBar, \
    QLineEdit, QMessageBox, QComboBox

WINDOW_TITLE = "ぺやんぐイベントマネージャー"

FILTERS = {
    "IMPLEMENTED": {
        "displayName": "実装したやつ",
        "filter": lambda event: event["implemented"]
    },
    "NON_IMPLEMENTED": {
        "displayName": "実装してないやつ",
        "filter": lambda event: not event["implemented"]
    },
    "ALL": {
        "displayName": "ぜんぶ",
        "filter": lambda event: True
    }
}

PRIORITIES = {
    "HIGHEST": {
        "displayName": "最優先",
        "value": 0
    },
    "HIGH": {
        "displayName": "優先",
        "value": 50
    },
    "NORMAL": {
        "displayName": "普通",
        "value": 100
    },
    "LOW": {
        "displayName": "後回し",
        "value": 150
    },
    "FUCKING_LAZY": {
        "displayName": "実装見送り",
        "value": 999
    }
}


def setupFilteringByPriorities():
    for key, value in PRIORITIES.items():
        FILTERS["PRIORITY_" + key] = {
            "displayName": "優先度：" + value["displayName"],
            "filter": lambda event, priority=key: event["priority"] == priority
        }


setupFilteringByPriorities()


def getPriorityNameFromInt(priorityInt):
    for key, value in PRIORITIES.items():
        if value["value"] == priorityInt:
            return key


class MainWindow(QMainWindow):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle(WINDOW_TITLE)

        self.resize(800, 600)

        self.centralWidget = QWidget()
        self.setCentralWidget(self.centralWidget)

        self.mainLayout = QVBoxLayout(self.centralWidget)
        self.tableLayout = QHBoxLayout()
        self.buttonLayout = QHBoxLayout()

        self.tableWidget = QTableWidget()
        self.tableWidget.setColumnCount(4)
        self.tableWidget.setHorizontalHeaderLabels(["実装", "名前", "優先度", "説明"])
        self.tableWidget.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)

        self.loadAction = QAction("ロード", self)
        self.loadAction.triggered.connect(self.loadJson)
        self.saveAction = QAction("保存", self)
        self.saveAction.triggered.connect(self.saveJson)
        self.saveAction.setEnabled(False)

        self.menuBar = QMenuBar(self)
        self.fileMenu = self.menuBar.addMenu("ファイル (&F)")
        self.fileMenu.addAction(self.loadAction)
        self.fileMenu.addAction(self.saveAction)
        self.setMenuBar(self.menuBar)

        self.viewMenu = self.menuBar.addMenu("表示 (&V)")
        self.addFilterSelectBoxes(self.viewMenu)
        self.viewMenu.setEnabled(False)

        self.applyButton = QPushButton("Apply (&A)")
        self.applyButton.clicked.connect(self.applyChanges)
        self.applyButton.setEnabled(False)
        self.buttonLayout.addStretch()
        self.buttonLayout.addWidget(self.applyButton)

        self.loadAction = QAction("いまの情報", self)
        self.loadAction.triggered.connect(self.showSummary)
        self.loadAction.setEnabled(False)
        self.helpMenu = self.menuBar.addMenu("ヘルプ (&H)")
        self.helpMenu.addAction(self.loadAction)

        self.searchLineEdit = QLineEdit()
        self.searchLineEdit.setPlaceholderText("検索する文字を入力してね！")
        self.searchButton = QPushButton("検索する！")
        self.searchButton.clicked.connect(self.search)
        self.searchLayout = QHBoxLayout()
        self.searchLayout.addWidget(self.searchLineEdit)
        self.searchLayout.addWidget(self.searchButton)

        self.mainLayout.addLayout(self.searchLayout)
        self.mainLayout.addLayout(self.tableLayout)
        self.mainLayout.addLayout(self.buttonLayout)
        self.tableLayout.addWidget(self.tableWidget)
        self.tableWidget.itemChanged.connect(self.onChange)

        self.eventsData = []
        self.eventsPath = None
        self.filters = [FILTERS["ALL"]]

        self.modified = False
        self.unsavedChanges = False

    def addFilterSelectBoxes(self, viewMenu):
        prioritySection = False

        for filterName, filterData in FILTERS.items():
            if filterName == "ALL":
                continue  # ぜんぶは表示しない
            elif filterName.startswith("PRIORITY_") and not prioritySection:
                prioritySection = True
                viewMenu.addSeparator()

            checkbox = QAction(filterData["displayName"], self, checkable=True)
            checkbox.setCheckable(True)

            checkbox.triggered.connect(lambda state, filterID=filterName:
                                       self.onFilterSelect(state, filterID)
                                       )

            viewMenu.addAction(checkbox)

    def onFilterSelect(self, state, filterName):
        if state:
            self.filters.append(FILTERS[filterName])
        else:
            self.filters.remove(FILTERS[filterName])

        if len(self.filters) == 0:
            self.filters.append(FILTERS["ALL"])

        for i, event in enumerate(self.eventsData):
            if all(f["filter"](event) for f in self.filters):
                self.tableWidget.setRowHidden(i, False)
            else:
                self.tableWidget.setRowHidden(i, True)

    def showSummary(self):
        summary = f"イベント数：{len(self.eventsData)}\n"
        implemented = 0
        notImplemented = 0
        for event in self.eventsData:
            if event["implemented"]:
                implemented += 1
            else:
                notImplemented += 1
        implPercentage = implemented / notImplemented * 100

        groupedEvents = self.groupByPriority()
        notImplementedFL = 0
        for fl in groupedEvents["FUCKING_LAZY"]:
            if not fl["implemented"]:
                notImplementedFL += 1

        summary += f"実装じょうきょう：{implemented}/{notImplemented - implemented} ({round(implPercentage, 2)}%)\n"

        summary += f"    うち\n"
        for name, priority in groupedEvents.items():
            notImplSum = 0
            implSum = 0
            for gEvt in groupedEvents[name]:
                if gEvt["implemented"]:
                    implSum += 1
                else:
                    notImplSum += 1

            implPercent = implSum / notImplSum * 100
            summary += f"    - {PRIORITIES[name]['displayName']}： {implSum}/{notImplSum} ({round(implPercent, 2)}%)\n"

        if len(self.eventsData) > 0:
            percent = implemented / len(self.eventsData) * 100
            summary += f"\n今の進捗：{round(percent, 2)}%\n\n"

            if implemented == len(self.eventsData):
                summary += "おめでとう！全部実装したね！頑張ったなお前！"
            elif implemented == 0:
                summary += "働けニート！"
            elif percent > 80:
                summary += "あとちょっとだな！頑張れ！"
            elif percent > 50:
                summary += "折返し地点だな！もっと仕事しろ！"
            elif percent > 20:
                summary += "まだまだだな！もっと頑張れ！"
            else:
                summary += "働け～！！！"

        QMessageBox.information(self, "いまの情報", summary)

    def groupByPriority(self):
        result = {}

        for priorityKey, value in PRIORITIES.items():
            result[priorityKey] = []

            for event in self.eventsData:
                if priorityKey == event["priority"]:
                    result[priorityKey].append(event)
        return result

    def search(self):
        search_text = self.searchLineEdit.text()
        if not search_text:
            QMessageBox.information(self, "検索する文字を入力してね！", "検索する文字を入力してね！")

        rowCount = self.tableWidget.rowCount()

        itemsToHide = []
        itemsToShow = [*range(rowCount)]

        found = False
        for i in range(rowCount):
            name = self.tableWidget.item(i, 1)
            foundOne = search_text.lower() not in name.text().lower()

            if foundOne:
                itemsToHide.append(i)
                itemsToShow.remove(i)
            elif not found:
                found = True

        if not found and search_text:
            QMessageBox.critical(self, "見つからなかったよ！", "お前が指定した名前のイベントは見つからなかったよ！")

        for i in itemsToHide:
            self.tableWidget.hideRow(i)
        for i in itemsToShow:
            self.tableWidget.showRow(i)

    def applyChanges(self):
        rowCount = self.tableWidget.rowCount()
        for i in range(rowCount):
            checkbox = self.tableWidget.item(i, 0)
            self.eventsData[i]["implemented"] = checkbox.checkState() == Qt.Checked

            priority = self.tableWidget.cellWidget(i, 2)
            priorityName = priority.itemData(priority.currentIndex())
            self.eventsData[i]["priority"] = priorityName

        self.saveAction.setEnabled(True)
        self.applyButton.setEnabled(False)

        self.modified = False
        self.unsavedChanges = True
        self.updateWindowTitle()

    def loadJson(self):
        fileName, _ = QFileDialog.getOpenFileName(self, "JSON を開く！", "", "JSON ふぁいる (*.json)")
        if fileName:
            try:
                with open(fileName, 'r', encoding='utf-8') as file:
                    self.eventsData = json.load(file)
                    self.eventsPath = fileName

                    self.normalizeLoadedData()
                    self.populateTable()

                    self.saveAction.setEnabled(False)
                    self.applyButton.setEnabled(False)
                    self.viewMenu.setEnabled(True)
                    self.loadAction.setEnabled(True)

                    self.modified = False
                    self.unsavedChanges = False
                    self.updateWindowTitle()
            except Exception as e:
                QMessageBox.critical(self, "Error", f"JSON を開けなかったみたい！ごめんね>< :\n{e}")

    def normalizeLoadedData(self):
        for event in self.eventsData:
            if "implemented" not in event:
                event["implemented"] = False
            if "priority" not in event:
                event["priority"] = "NORMAL"

    def onChange(self, item):
        self.applyButton.setEnabled(True)

        self.modified = True
        self.unsavedChanges = True
        self.updateWindowTitle()

    def populateTable(self):
        events = self.eventsData

        while self.tableWidget.rowCount() > 0:
            self.tableWidget.removeRow(0)

        rowCount = len(events)
        self.tableWidget.setRowCount(rowCount)

        for count, item in enumerate(events):
            checkbox = QTableWidgetItem()
            checkbox.setFlags(Qt.ItemIsUserCheckable | Qt.ItemIsEnabled)
            checkbox.setCheckState(Qt.Checked if item["implemented"] else Qt.Unchecked)

            nameItem = QTableWidgetItem(item["name"])
            descriptionItem = QTableWidgetItem(item["description"])

            # Read-only
            nameItem.setFlags(nameItem.flags() ^ Qt.ItemIsEditable)
            descriptionItem.setFlags(descriptionItem.flags() ^ Qt.ItemIsEditable)

            self.tableWidget.setItem(count, 0, checkbox)
            self.tableWidget.setItem(count, 1, nameItem)
            self.tableWidget.setCellWidget(count, 2, self.createPriorityComboBox(item["priority"]))
            self.tableWidget.setItem(count, 3, descriptionItem)

        self.tableWidget.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeToContents)
        self.tableWidget.resizeColumnsToContents()

    def createPriorityComboBox(self, currentPriority):
        comboBox = QComboBox()
        for key, value in PRIORITIES.items():
            comboBox.addItem(value["displayName"], key)

        comboBox.setCurrentText(PRIORITIES[currentPriority]["displayName"])
        comboBox.currentIndexChanged.connect(self.onChange)
        return comboBox

    def saveJson(self):
        if self.modified:
            reply = QMessageBox.information(self, "変更が適用されていません!", "適用する場合は「はい」を、"
                                                                       "前回適用分までのみ保存する場合は「いいえ」を、"
                                                                       "キャンセルする場合は「キャンセル」を押してください。",
                                            QMessageBox.Yes | QMessageBox.No | QMessageBox.Cancel)
            if reply == QMessageBox.Yes:
                self.applyChanges()
            elif reply == QMessageBox.Cancel:
                return

        fileName = self.eventsPath
        if not fileName:
            fileName, _ = QFileDialog.getSaveFileName(self, "JSON に保存するよ！", "", "JSON ふぁいる (*.json)")
        if fileName:
            try:
                with open(fileName, 'w', encoding='utf-8') as file:
                    json.dump(self.eventsData, file, ensure_ascii=False)
                    self.saveAction.setEnabled(False)
                    QMessageBox.information(self, "保存完了！", "保存したよ！")
                    self.showSummary()

                    self.unsavedChanges = False
                    self.updateWindowTitle()
            except Exception as e:
                QMessageBox.critical(self, "Error", f"JSON を保存できなかったみたい！ごめんね>< :\n{e}")

    def updateWindowTitle(self):
        title = WINDOW_TITLE
        if self.unsavedChanges:
            title += " [未保存]"

        if self.modified:
            title += " [未適用]"

        self.setWindowTitle(title)


if __name__ == '__main__':
    import sys

    app = QApplication(sys.argv)

    mainWindow = MainWindow()
    mainWindow.show()

    sys.exit(app.exec_())

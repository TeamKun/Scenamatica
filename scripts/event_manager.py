import json
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QTableWidget, \
    QTableWidgetItem, QHeaderView, QPushButton, QFileDialog, QAction, QMenuBar, \
    QLineEdit, QMessageBox


class MainWindow(QMainWindow):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Events JSON Editor")

        self.resize(800, 600)

        self.centralWidget = QWidget()
        self.setCentralWidget(self.centralWidget)

        self.mainLayout = QVBoxLayout(self.centralWidget)
        self.tableLayout = QHBoxLayout()
        self.buttonLayout = QHBoxLayout()


        self.tableWidget = QTableWidget()
        self.tableWidget.setColumnCount(3)
        self.tableWidget.setHorizontalHeaderLabels(["実装したやつ", "名前", "説明"])
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

        self.impleOnlyAction = QAction("実装したやつのみ", self)
        self.impleOnlyAction.setCheckable(True)
        self.impleOnlyAction.triggered.connect(lambda: self.populateTable())
        self.unimpleOnlyAction = QAction("実装されてないやつのみ", self)
        self.unimpleOnlyAction.setCheckable(True)
        self.unimpleOnlyAction.triggered.connect(lambda: self.populateTable())

        self.viewMenu = self.menuBar.addMenu("表示 (&V)")
        self.viewMenu.addAction(self.impleOnlyAction)
        self.viewMenu.addAction(self.unimpleOnlyAction)

        self.applyButton = QPushButton("Apply (&A)")
        self.applyButton.clicked.connect(self.applyChanges)
        self.applyButton.setEnabled(False)
        self.buttonLayout.addStretch()
        self.buttonLayout.addWidget(self.applyButton)

        self.loadAction = QAction("いまの情報", self)
        self.loadAction.triggered.connect(self.showSummary)
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
        self.tableWidget.itemChanged.connect(self.enableApplyButton)

        self.eventsData = []
        self.eventsPath = None

    def showSummary(self):
        summary = f"イベント数：{len(self.eventsData)}\n"
        implemented = 0
        for event in self.eventsData:
            if event["implemented"]:
                implemented += 1
        summary += f"実装済み：{implemented}\n"
        summary += f"してない奴：{len(self.eventsData) - implemented}\n"

        if len(self.eventsData) > 0:
            summary += "\n\n"
            percent = implemented / len(self.eventsData) * 100
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

    def search(self):
        search_text = self.searchLineEdit.text()
        if not search_text:
            QMessageBox.information(self, "検索する文字を入力してね！", "検索する文字を入力してね！")
            return

        rowCount = self.tableWidget.rowCount()
        found = False
        for i in range(rowCount):
            name = self.tableWidget.item(i, 1)
            if search_text.lower() in name.text().lower():
                self.tableWidget.selectRow(i)
                found = True
        if not found:
            QMessageBox.critical(self, "見つからなかったよ！", "お前が指定した名前のイベントは見つからなかったよ！")

    def applyChanges(self):
        rowCount = self.tableWidget.rowCount()
        for i in range(rowCount):
            checkbox = self.tableWidget.item(i, 0)
            self.eventsData[i]["implemented"] = checkbox.checkState() == Qt.Checked
        self.saveAction.setEnabled(True)
        self.applyButton.setEnabled(False)

    def loadJson(self):
        fileName, _ = QFileDialog.getOpenFileName(self, "JSON を開く！", "", "JSON ふぁいる (*.json)")
        if fileName:
            try:
                with open(fileName, 'r', encoding='utf-8') as file:
                    self.eventsData = json.load(file)
                    self.eventsPath = fileName
                    self.populateTable()
                    self.saveAction.setEnabled(False)
                    self.applyButton.setEnabled(False)
            except Exception as e:
                QMessageBox.critical(self, "Error", f"JSON を開けなかったみたい！ごめんね>< :\n{e}")

    def enableApplyButton(self, item):
        self.applyButton.setEnabled(True)

    def populateTable(self):
        show_impl = self.impleOnlyAction.isChecked()
        show_unImpl = self.unimpleOnlyAction.isChecked()

        if not show_impl and not show_unImpl:
            show_impl = True
            show_unImpl = True

        rowCount = len(self.eventsData)
        self.tableWidget.setRowCount(rowCount)
        filtered_data = []
        if show_impl and show_unImpl:
            filtered_data = self.eventsData
        else:
            for item in self.eventsData:
                if show_impl and item["implemented"]:
                    filtered_data.append(item)
                elif show_unImpl and not item["implemented"]:
                    filtered_data.append(item)
            rowCount = len(filtered_data)
            self.tableWidget.setRowCount(rowCount)
        for i, item in enumerate(filtered_data):
            checkbox = QTableWidgetItem()
            checkbox.setFlags(Qt.ItemIsUserCheckable | Qt.ItemIsEnabled)
            checkbox.setCheckState(Qt.Checked if item["implemented"] else Qt.Unchecked)
            self.tableWidget.setItem(i, 0, checkbox)
            self.tableWidget.setItem(i, 1, QTableWidgetItem(item["name"]))
            self.tableWidget.setItem(i, 2, QTableWidgetItem(item["description"]))

        self.tableWidget.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeToContents)
        self.tableWidget.resizeColumnsToContents()

    def saveJson(self):
        fileName = self.eventsPath
        if not fileName:
            fileName, _ = QFileDialog.getSaveFileName(self, "JSON に保存するよ！", "", "JSON ふぁいる (*.json)")
        if fileName:
            try:
                data = self.eventsData  # 読み込んだデータを参照
                with open(fileName, 'w', encoding='utf-8') as file:
                    json.dump(data, file, ensure_ascii=False)
                    self.saveAction.setEnabled(False)
                    QMessageBox.information(self, "保存完了！", "保存したよ！")
                    self.showSummary()
            except Exception as e:
                QMessageBox.critical(self, "Error", f"JSON を保存できなかったみたい！ごめんね>< :\n{e}")


if __name__ == '__main__':
    import sys

    app = QApplication(sys.argv)

    mainWindow = MainWindow()
    mainWindow.show()

    sys.exit(app.exec_())

export interface Type {
  displayString: string

  referenceString: string | null

}

export const isType = (value: any): value is Type => {
  return value.displayString !== undefined && value.referenceString !== undefined
}

export const PrimitiveTypes: { [key: string]: Type } = {
  OBJECT: {
    displayString: "構造体",
    referenceString: null,
  },
  STRING: {
    displayString: "文字列",
    referenceString: null,
  },
  INTEGER: {
    displayString: "整数値 (32bit)",
    referenceString: null,
  },
  LONG: {
    displayString: "整数値 (64bit)",
    referenceString: null,
  },
  FLOAT: {
    displayString: "単精度浮動小数点数",
    referenceString: null,
  },
  DOUBLE: {
    displayString: "倍精度浮動小数点数",
    referenceString: null,
  },
  BOOLEAN: {
    displayString: "真偽値",
    referenceString: null,
  },
}

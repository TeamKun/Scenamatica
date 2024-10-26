package org.kunlab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YAMLTypeMismatchException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.yaml.snakeyaml.nodes.Node;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 構造化された YAML ノードを表します。
 */
public interface StructuredYamlNode
{
    /**
     * このノードが指定された型であるかどうかを返します。
     * また, {@link YAMLNodeType#STRING} を指定し, かつ自値が ScalarNode である場合は, 中身のいかんに関係なく {@code true} を返します。
     *
     * @param type 型
     * @return このノードが指定された型である場合は true, それ以外の場合は false
     */
    boolean isType(YAMLNodeType type);

    /**
     * このノードが文字列型である場合、その値を返します。
     *
     * @return このノードが文字列型である場合の値
     */
    String asString();

    /**
     * このノードが文字列型である場合、その値を返します。
     *
     * @param defaultValue デフォルト値
     * @return このノードが文字列型である場合の値]
     */
    String asString(String defaultValue);

    /**
     * このノードが整数型である場合、その値を返します。
     *
     * @return このノードが整数型である場合の値
     * @throws YAMLTypeMismatchException このノードが整数型でない場合
     */
    Integer asInteger() throws YAMLTypeMismatchException;

    /**
     * このノードが整数型である場合、その値を返します。
     *
     * @param defaultValue デフォルト値
     * @return このノードが整数型である場合の値
     * @throws YAMLTypeMismatchException このノードが整数型でない場合
     */
    Integer asInteger(Integer defaultValue) throws YAMLTypeMismatchException;

    /**
     * このノードが整数型である場合、その値を返します。
     *
     * @return このノードが整数型である場合の値
     * @throws YAMLTypeMismatchException このノードが整数型でない場合
     */
    Long asLong() throws YAMLTypeMismatchException;

    /**
     * このノードが整数型である場合、その値を返します。
     *
     * @param defaultValue デフォルト値
     * @return このノードが整数型である場合の値
     * @throws YAMLTypeMismatchException このノードが整数型でない場合
     */
    Long asLong(Long defaultValue) throws YAMLTypeMismatchException;


    /**
     * このノードが真偽値型である場合、その値を返します。
     *
     * @return このノードが真偽値型である場合の値
     * @throws YAMLTypeMismatchException このノードが真偽値型でない場合
     */
    Boolean asBoolean() throws YAMLTypeMismatchException;

    /**
     * このノードが真偽値型である場合、その値を返します。
     *
     * @param defaultValue デフォルト値
     * @return このノードが真偽値型である場合の値
     * @throws YAMLTypeMismatchException このノードが真偽値型でない場合
     */
    Boolean asBoolean(Boolean defaultValue) throws YAMLTypeMismatchException;

    /**
     * このノードが浮動小数点数型である場合、その値を返します。
     *
     * @return このノードが浮動小数点数型である場合の値
     * @throws YAMLTypeMismatchException このノードが浮動小数点数型でない場合
     */
    Float asFloat() throws YAMLTypeMismatchException;

    /**
     * このノードが浮動小数点数型である場合、その値を返します。
     *
     * @param defaultValue デフォルト値
     * @return このノードが浮動小数点数型である場合の値
     * @throws YAMLTypeMismatchException このノードが浮動小数点数型でない場合
     */
    Float asFloat(Float defaultValue) throws YAMLTypeMismatchException;

    /**
     * このノードが浮動小数点数型である場合、その値を返します。
     *
     * @return このノードが浮動小数点数型である場合の値
     * @throws YAMLTypeMismatchException このノードが浮動小数点数型でない場合
     */
    Double asDouble() throws YAMLTypeMismatchException;

    /**
     * このノードがバイト型である場合、その値を返します。
     *
     * @return このノードがバイト型である場合の値
     * @throws YAMLTypeMismatchException このノードがバイト型でない場合
     */
    Byte asByte() throws YAMLTypeMismatchException;

    /**
     * このノードがバイナリ型である場合、その値を返します。
     *
     * @return このノードがバイナリ型である場合の値
     * @throws YAMLTypeMismatchException このノードがバイナリ型でない場合
     */
    Byte[] asBinary() throws YAMLTypeMismatchException;

    /**
     * このノードが null 型である場合、true を返します。
     *
     * @return このノードが null 型である場合は true, それ以外の場合は false
     * @throws YAMLTypeMismatchException このノードが null 型でない場合
     */
    boolean isNull() throws YAMLTypeMismatchException;

    /**
     * このノードがリスト型である場合、その値を返します。
     *
     * @return このノードがリスト型である場合の値
     * @throws YAMLTypeMismatchException このノードがリスト型でない場合
     */
    List<StructuredYamlNode> asList() throws YAMLTypeMismatchException;

    /**
     * このノードがリスト型である場合、その値を返します。
     *
     * @param mapper マッピング関数
     * @return このノードがリスト型である場合の値
     * @throws YamlParsingException このノードがリスト型でない場合や, マッピング関数が失敗した場合
     */
    <T> List<T> asList(ValueMapper<T> mapper) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、その値を返します。
     *
     * @param keyMapper   キーのマッピング関数
     * @param valueMapper 値のマッピング関数
     * @param <K>         キーの型
     * @param <V>         値の型
     * @return このノードが Map 型である場合の値
     * @throws YamlParsingException このノードが Map 型でない場合や, マッピング関数が失敗した場合
     */
    @NotNull
    <K, V> Map<K, V> asMap(ValueMapper<K> keyMapper, ValueMapper<V> valueMapper) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、その値を返します。
     * 値は Map&lt;String, Object&gt; として返されます。
     *
     * @return このノードが Map 型である場合の値
     * @throws YamlParsingException このノードが Map 型でない場合や, マッピング関数が失敗した場合
     */
    @NotNull
    Map<String, Object> asMap() throws YamlParsingException;

    /**
     * このノードが Map 型である場合、その値を返します。
     * 値は Map&lt;StructuredYamlNode, StructuredYamlNode&gt; として返されます。
     *
     * @return このノードが Map 型である場合の値
     * @throws YamlParsingException このノードが Map 型でない場合や, マッピング関数が失敗した場合
     */
    @NotNull
    Map<StructuredYamlNode, StructuredYamlNode> asNodeMap() throws YamlParsingException;


    /**
     * このノードを、その型に応じたオブジェクトとして返します。
     *
     * @return このノードを表すオブジェクト
     */
    Object asObject() throws YamlParsingException;

    /**
     * このノードがリスト型である場合、その値を Stream として返します。
     *
     * @return このノードがリスト型である場合の値
     * @throws YAMLTypeMismatchException このノードがリスト型でない場合
     */
    Stream<StructuredYamlNode> asSequenceStream() throws YAMLTypeMismatchException;

    /**
     * このノードがリスト型である場合、指定された値を追加します。
     *
     * @param item 追加する値
     * @throws YAMLTypeMismatchException このノードがリスト型でない場合
     */
    void addSequenceItem(StructuredYamlNode item) throws YAMLTypeMismatchException;

    /**
     * このノードがリスト型である場合、指定された値を削除します。
     *
     * @param item 削除する値
     * @throws YAMLTypeMismatchException このノードがリスト型でない場合
     */
    void removeSequenceItem(StructuredYamlNode item) throws YAMLTypeMismatchException;

    /**
     * このノードが Map 型である場合、すべてのエントリを返します。
     *
     * @return このノードが Map 型である場合のエントリ
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    List<? extends Pair<? extends StructuredYamlNode, ? extends StructuredYamlNode>> getMappingEntries() throws YAMLTypeMismatchException;

    /**
     * このノードがマップ型もしくはリスト型である場合、その値をすべて削除します。
     */
    void clearItems();

    /**
     * このノードが格納されるファイルの内容を返します。
     *
     * @return このノードが格納されるファイルの内容
     */
    @NotNull String getFileContent();

    /**
     * このノードが格納されるファイルの名前を返します。
     *
     * @return このノードが格納されるファイルの名前
     */
    @NotNull String getFileName();

    /**
     * このノードの開始行を返します。
     *
     * @return このノードの開始行
     */
    int getStartLine();

    /**
     * このノードの終了行を返します。
     *
     * @return このノードの終了行
     */
    int getEndLine();

    /**
     * スカラ値を変更します。
     *
     * @param scalarType スカラ値の型
     * @param value      新しい値
     * @return このノード
     */
    @Contract("_, _ -> this")
    StructuredYamlNode changeScalarValue(YAMLNodeType scalarType, Object value);

    /**
     * このノードが Map 型である場合、指定されたキーが存在するかどうかを返します。
     *
     * @param key キー
     * @return 指定されたキーが存在する場合は true, それ以外の場合は false
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    boolean containsKey(Object key) throws YAMLTypeMismatchException;

    /**
     * このノードが Map 型である場合、指定されたキーのペアを削除します。
     * キーが存在しない場合は何もしません。
     *
     * @param key キー
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    void remove(Object key) throws YAMLTypeMismatchException;

    /**
     * このノードが Map 型である場合、指定されたキーと値を追加します。
     *
     * @param key   キー
     * @param value 値
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    void add(StructuredYamlNode key, StructuredYamlNode value) throws YAMLTypeMismatchException;

    /**
     * このノードが Map 型である場合、２つのマッピングをマージします。
     * 与えられたノードが Map 型出ない場合, {@link IllegalArgumentException} がスローされます。
     *
     * @param other マージするマッピング
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    void mergeMapping(StructuredYamlNode other) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、指定されたキーに対応する値を返します。
     * キーに対応する値が存在しない場合は null を示すノードを返します。
     *
     * @param key キー
     * @return 指定されたキーに対応する値
     * @throws YamlParsingException このノードが Map 型でない場合
     */
    StructuredYamlNode get(Object key) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、指定されたキーの値が指定された型であるかどうかを検証します。
     *
     * @param type 型
     * @throws YamlParsingException このノードが Map 型でない場合や, 指定されたキーが存在しない場合
     */
    void ensureTypeOf(YAMLNodeType... type) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、指定されたキーの値が指定された型であるかどうかを検証します。
     *
     * @param type 型
     * @throws YamlParsingException このノードが Map 型でない場合。
     */
    void ensureTypeOfIfExists(YAMLNodeType type) throws YamlParsingException;

    /**
     * このノードが Map 型である場合、要素数を返します。
     * このノードが List 型である場合、要素数を返します。
     * このノードが String 型である場合、文字数を返します。
     * その他は 0 を返します。
     *
     * @return 要素数または文字数
     */
    int size();

    /**
     * このノードが Map 型である場合、キーの一覧を返します。
     *
     * @return キーの一覧
     * @throws YAMLTypeMismatchException このノードが Map 型でない場合
     */
    List<StructuredYamlNode> keys() throws YAMLTypeMismatchException;

    /**
     * このノードがリスト型である場合、指定されたインデックスに対応する値を返します。
     *
     * @param index インデックス
     * @return 指定されたインデックスに対応する値
     * @throws YamlParsingException このノードがリスト型でない場合や, 指定されたインデックスが存在しない場合
     */
    StructuredYamlNode getItem(int index) throws YamlParsingException;

    /**
     * このノードの親ノードを返します。
     *
     * @return このノードの親ノード
     */
    @NotNull
    StructuredYamlNode getRoot();

    /**
     * このノードを表す SnakeYAML ノードを返します。
     *
     * @return このノードを表す SnakeYAML ノード
     */
    Node getThisNode();

    /**
     * この値をマッピングして返します。
     *
     * @param mapper 値を変換する関数
     * @param <T>    値の型
     * @return 指定されたキーに対応する値
     * @throws YamlParsingException このノードが Map 型でない場合や, 指定されたキーが存在しない場合
     */
    <T> T getAs(ValueMapper<T> mapper) throws YamlParsingException;

    /**
     * この値をマッピングして返します。
     *
     * @param mapper       値を変換する関数
     * @param defaultValue デフォルト値
     * @param <T>          値の型
     * @return 指定されたキーに対応する値
     * @throws YamlParsingException このノードが Map 型でない場合
     */
    <T> T getAs(ValueMapper<T> mapper, T defaultValue) throws YamlParsingException;

    /**
     * このノードがマッピング型である場合、指定された値を実際に変形して, 正しい値であるかどうかを検証します。
     * 値が存在しない場合は例外をスローします。
     *
     * @param validator 検証関数
     * @param message   検証に失敗した場合にスローされる例外のメッセージ
     * @throws YamlParsingException このノードが Map 型でない場合や, 検証に失敗した場合
     */
    void validate(Validator validator, String message) throws YamlParsingException;

    /**
     * このノードがマッピング型である場合、指定された値を実際に変形して, 正しい値であるかどうかを検証します。
     * 値が存在しない場合はスルーします。
     *
     * @param validator 検証関数
     * @throws YamlParsingException このノードが Map 型でない場合や, 検証に失敗した場合
     */
    void validateIfExists(Validator validator) throws YamlParsingException;

    /**
     * このノードがマッピング型である場合、指定された値を実際に変形して, 正しい値であるかどうかを検証します。
     * 値が存在しない場合はスルーします。
     *
     * @param validator 検証関数
     * @param message   検証に失敗した場合にスローされる例外のメッセージ
     * @throws YamlParsingException このノードが Map 型でない場合や, 検証に失敗した場合
     */
    void validateIfExists(Validator validator, @Nullable String message) throws YamlParsingException;

    interface ValueMapper<T>
    {
        T map(StructuredYamlNode node) throws Exception;
    }

    interface Validator
    {
        Object validate(StructuredYamlNode node) throws Exception;

        /**
         * Validate 関数にメッセージを指定しなかったときに使用されるメッセージを返します。
         *
         * @return メッセージ
         */
        default String getMessage()
        {
            return null;
        }
    }
}

package org.kunlab.scenamatica.exceptions.scenariofile;

import lombok.Getter;

import java.util.function.Function;

public class UnknownEnumValueException extends YamlValueMappingException
{
    @Getter
    private final String actualValue;
    private final MessageProvider<?> messageProvider;

    public <T extends Enum<?>> UnknownEnumValueException(String message, String fileName, String targetKey, int line, String[] aroundLines, String actualValue, Class<T> enumClass, Function<T, String> identifier)
    {
        super(message, fileName, targetKey, line, aroundLines);

        this.actualValue = actualValue;
        this.messageProvider = new MessageProvider<>(enumClass, identifier);
    }

    @Override
    public String getMessage()
    {
        return this.messageProvider.getMessage(this.actualValue);
    }

    private static class MessageProvider<T extends Enum<?>>
    {
        private final Class<T> enumClass;
        private final Function<T, String> identifier;

        public MessageProvider(Class<T> enumClass, Function<T, String> identifier)
        {
            this.enumClass = enumClass;
            this.identifier = identifier;
        }

        public String getMessage(String actualValue)
        {
            StringBuilder enumValues = new StringBuilder("]");
            for (T value : this.enumClass.getEnumConstants())
            {
                if (enumValues.length() > 1)
                    enumValues.append(", ");

                if (value.ordinal() > 5)
                {
                    enumValues.append("...");
                    break;
                }
                else
                    enumValues.append(this.identifier.apply(value));
            }

            enumValues.append(']');

            return "[!] An enum value of '" + this.enumClass.getSimpleName() + "' is expected.\n"
                    + "[!] Expected values: " + enumValues + ", but got '" + actualValue + "'.";
        }
    }
}

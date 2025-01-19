package com.falkordb.client;

import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    public static String encode(final Object data) {
        if (data instanceof byte[]) {
            return new String(( byte[])data, StandardCharsets.UTF_8);
        }
        return String.valueOf(data);
    }


    public static List<Double> toListOfDouble(Object data) {

        @SuppressWarnings("unchecked") List<String> values = (List<String>) data;
        return values.stream().map(Double::parseDouble).collect(Collectors.toList());

    }

    /**
     * Prepare and format a procedure call and its arguments
     * @param procedure - procedure to invoke
     * @param args - procedure arguments
     * @param kwargs - procedure output arguments
     * @return formatter procedure call
     */
    public static String prepareProcedure(String procedure, List<String> args  , Map<String, List<String>> kwargs){
        args = args.stream().map( Util::quoteString).collect(Collectors.toList());
        StringBuilder queryStringBuilder =  new StringBuilder();
        queryStringBuilder.append("CALL ").append(procedure).append('(');
        int i = 0;
        for (; i < args.size() - 1; i++) {
            queryStringBuilder.append(args.get(i)).append(',');
        }
        if (i == args.size()-1) {
            queryStringBuilder.append(args.get(i));
        }
        queryStringBuilder.append(')');
        List<String> kwargsList = kwargs.getOrDefault("y", null);
        if(kwargsList != null){
            i = 0;
            for (; i < kwargsList.size() - 1; i++) {
                queryStringBuilder.append(kwargsList.get(i)).append(',');

            }
            queryStringBuilder.append(kwargsList.get(i));
        }
        return queryStringBuilder.toString();
    }

    private static String quoteString(String str){
        return '"' +
                str.replace("\"", "\\\"") +
                '"';
    }

    public static String prepareQuery(String query, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("CYPHER ");
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sb.append(key).append('=');
            sb.append(valueToString(value));
            sb.append(' ');
        }
        sb.append(query);
        return sb.toString();
    }

    private static String valueToString(@Nullable Object value) {
        if(value == null) return "null";

        if(value instanceof String){
            return quoteString((String) value);
        }
        if(value instanceof Character){
            return quoteString(((Character)value).toString());
        }

        if(value instanceof Object[]){
            return arrayToString((Object[]) value);

        }
        if(value instanceof List){
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return arrayToString(list.toArray());
        }
        return value.toString();
    }

    private static String arrayToString(Object[] arr) {
        return '[' +
                Arrays.stream(arr).map(Util::valueToString).collect(Collectors.joining(", ")) +
                ']';
    }


}

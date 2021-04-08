package com.reign.framework.jdbc;

/**
 * @ClassName: DefaultNameStrategy
 * @Description: 默认名称转换策略
 * ----------------------------------
 * |  columnName   |   propertyName |
 * ---------------------------------
 * |  player_id    |   playerId    |
 * ---------------------------------
 * @Author: wuwx
 * @Date: 2021-04-02 14:56
 **/
public class DefaultNameStrategy implements NameStrategy {

    /**
     * 列->类属性
     *
     * @param colomnName
     * @return
     */
    @Override
    public String columnNameToPropertyName(String colomnName) {
        StringBuilder builder = new StringBuilder(colomnName.length());
        boolean capital = false;
        for (int i = 0; i < colomnName.length(); i++) {
            char ch = colomnName.charAt(i);
            switch (ch) {
                case '_':
                    capital = true;
                    break;
                default:
                    if (capital) {
                        builder.append(Character.toUpperCase(ch));
                        capital = false;
                    } else {
                        builder.append(ch);
                    }
                    break;
            }
        }
        return builder.toString();
    }


    /**
     * 类属性->列
     *
     * @param propertyName
     * @return
     */
    @Override
    public String propertyNameToColumnName(String propertyName) {
        StringBuilder builder = new StringBuilder(propertyName.length() + 1);
        boolean capital = false;
        boolean first = true;
        for (int i = 0; i < propertyName.length(); i++) {
            char ch = propertyName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                capital = true;
            }

            if (capital && !first) {
                builder.append("_").append(Character.toLowerCase(ch));
                capital = false;
            } else if (capital) {
                first = false;
                capital = false;
                builder.append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}

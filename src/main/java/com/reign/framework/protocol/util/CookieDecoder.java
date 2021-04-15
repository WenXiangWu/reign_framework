package com.reign.framework.protocol.util;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.util.internal.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class CookieDecoder {
    private static final char COMMA = ',';

    public  Set<Cookie> decode(String header) {
        List<String> names = new ArrayList(8);
        List<String> values = new ArrayList(8);
        extractKeyValuePairs(header, names, values);
        if (names.isEmpty()) {
            return Collections.emptySet();
        } else {
            int version = 0;
            int i;
            if (((String)names.get(0)).equalsIgnoreCase("Version")) {
                try {
                    version = Integer.parseInt((String)values.get(0));
                } catch (NumberFormatException var28) {
                }

                i = 1;
            } else {
                i = 0;
            }

            if (names.size() <= i) {
                return Collections.emptySet();
            } else {
                TreeSet cookies;
                for(cookies = new TreeSet(); i < names.size(); ++i) {
                    String name = (String)names.get(i);
                    String value = (String)values.get(i);
                    if (value == null) {
                        value = "";
                    }

                    Cookie c = new DefaultCookie(name, value);
                    boolean discard = false;
                    boolean secure = false;
                    boolean httpOnly = false;
                    String comment = null;
                    String commentURL = null;
                    String domain = null;
                    String path = null;
                    long maxAge = -9223372036854775808L;
                    List<Integer> ports = new ArrayList(2);

                    for(int j = i + 1; j < names.size(); ++i) {
                        name = (String)names.get(j);
                        value = (String)values.get(j);
                        if ("Discard".equalsIgnoreCase(name)) {
                            discard = true;
                        } else if ("Secure".equalsIgnoreCase(name)) {
                            secure = true;
                        } else if ("HTTPOnly".equalsIgnoreCase(name)) {
                            httpOnly = true;
                        } else if ("Comment".equalsIgnoreCase(name)) {
                            comment = value;
                        } else if ("CommentURL".equalsIgnoreCase(name)) {
                            commentURL = value;
                        } else if ("Domain".equalsIgnoreCase(name)) {
                            domain = value;
                        } else if ("Path".equalsIgnoreCase(name)) {
                            path = value;
                        } else if ("Expires".equalsIgnoreCase(name)) {
                            try {
                                long maxAgeMillis = new CookieDateFormat().parse(value).getTime() - System.currentTimeMillis();
                                maxAge = maxAgeMillis / 1000L + (long)(maxAgeMillis % 1000L != 0L ? 1 : 0);
                            } catch (ParseException var27) {
                            }
                        } else if ("Max-Age".equalsIgnoreCase(name)) {
                            maxAge = (long)Integer.parseInt(value);
                        } else if ("Version".equalsIgnoreCase(name)) {
                            version = Integer.parseInt(value);
                        } else {
                            if (!"Port".equalsIgnoreCase(name)) {
                                break;
                            }

                            String[] portList = StringUtil.split(value, ',');
                            String[] arr$ = portList;
                            int len$ = portList.length;

                            for(int i$ = 0; i$ < len$; ++i$) {
                                String s1 = arr$[i$];

                                try {
                                    ports.add(Integer.valueOf(s1));
                                } catch (NumberFormatException var26) {
                                }
                            }
                        }

                        ++j;
                    }

                    c.setVersion(version);
                    c.setMaxAge(maxAge);
                    c.setPath(path);
                    c.setDomain(domain);
                    c.setSecure(secure);
                    c.setHttpOnly(httpOnly);
                    if (version > 0) {
                        c.setComment(comment);
                    }

                    if (version > 1) {
                        c.setCommentUrl(commentURL);
                        c.setPorts(ports);
                        c.setDiscard(discard);
                    }

                    cookies.add(c);
                }

                return cookies;
            }
        }
    }

    private static void extractKeyValuePairs(String header, List<String> names, List<String> values) {
        int headerLen = header.length();
        int i = 0;

        label78:
        while(i != headerLen) {
            switch(header.charAt(i)) {
                case '\t':
                case '\n':
                case '\u000b':
                case '\f':
                case '\r':
                case ' ':
                case ',':
                case ';':
                    ++i;
                    break;
                default:
                    while(i != headerLen) {
                        if (header.charAt(i) != '$') {
                            String name;
                            String value;
                            if (i == headerLen) {
                                name = null;
                                value = null;
                            } else {
                                int newNameStart = i;

                                label69:
                                while(true) {
                                    switch(header.charAt(i)) {
                                        case ';':
                                            name = header.substring(newNameStart, i);
                                            value = null;
                                            break label69;
                                        case '=':
                                            name = header.substring(newNameStart, i);
                                            ++i;
                                            if (i == headerLen) {
                                                value = "";
                                            } else {
                                                char c = header.charAt(i);
                                                if (c == '"' || c == '\'') {
                                                    StringBuilder newValueBuf = new StringBuilder(header.length() - i);
                                                    char q = c;
                                                    boolean hadBackslash = false;
                                                    ++i;

                                                    while(i != headerLen) {
                                                        if (hadBackslash) {
                                                            hadBackslash = false;
                                                            c = header.charAt(i++);
                                                            switch(c) {
                                                                case '"':
                                                                case '\'':
                                                                case '\\':
                                                                    newValueBuf.setCharAt(newValueBuf.length() - 1, c);
                                                                    break;
                                                                default:
                                                                    newValueBuf.append(c);
                                                            }
                                                        } else {
                                                            c = header.charAt(i++);
                                                            if (c == q) {
                                                                value = newValueBuf.toString();
                                                                break label69;
                                                            }

                                                            newValueBuf.append(c);
                                                            if (c == '\\') {
                                                                hadBackslash = true;
                                                            }
                                                        }
                                                    }

                                                    value = newValueBuf.toString();
                                                    break label69;
                                                }

                                                int semiPos = header.indexOf(59, i);
                                                if (semiPos > 0) {
                                                    value = header.substring(i, semiPos);
                                                    i = semiPos;
                                                } else {
                                                    value = header.substring(i);
                                                    i = headerLen;
                                                }
                                            }
                                            break label69;
                                        default:
                                            ++i;
                                            if (i == headerLen) {
                                                name = header.substring(newNameStart);
                                                value = null;
                                                break label69;
                                            }
                                    }
                                }
                            }

                            names.add(name);
                            values.add(value);
                            continue label78;
                        }

                        ++i;
                    }

                    return;
            }
        }

    }

    public CookieDecoder() {
        super();
    }

    final class CookieDateFormat extends SimpleDateFormat{
        private static final long serialVersionUid = 1L;

        public CookieDateFormat() {
            super("E, d-MMM-y HH:mm:ss z", Locale.ENGLISH);
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }
}

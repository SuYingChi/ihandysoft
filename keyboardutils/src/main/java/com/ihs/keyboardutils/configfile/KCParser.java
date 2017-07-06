package com.ihs.keyboardutils.configfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class KCParser {

    private static byte[] header = {'H', 'S', 'K', 'C', 1, 0};

    private KCParser() {

    }

    private static Object parse(byte[] bytes) {
        String string = null;
        if (checkFileHeader(bytes, header)) {
            string = decryptBytes(bytes, header.length);
        } else {
            try {
                string = new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        JSONTokener tokener = new JSONTokener(string);
        Object object = null;
        try {
            object = tokener.nextValue();
        } catch (JSONException e) {

        }
        if (object instanceof JSONObject) {
            return toMap((JSONObject)object);
        } else if (object instanceof JSONArray) {
            return toList((JSONArray)object);
        } else {
            return object;
        }
    }

    private static Object parse(InputStream inputStream) {
        byte[] bytes = readStreamAsBytes(inputStream);
        return parse(bytes);
    }

    private static Object parse(File inputFile) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
            return parse(inputStream);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {

            }
        }
    }

    public static KCMap parseMap(InputStream inputStream) {
        Object object = parse(inputStream);
        if (object instanceof KCMap) {
            return (KCMap)object;
        } else {
            return null;
        }
    }

    public static KCList parseList(InputStream inputStream) {
        Object object = parse(inputStream);
        if (object instanceof KCList) {
            return (KCList) object;
        } else {
            return null;
        }
    }

    public static KCMap parseMap(File inputFile) {
        Object object = parse(inputFile);
        if (object instanceof KCMap) {
            return (KCMap)object;
        } else {
            return null;
        }
    }

    public static KCList parseList(File inputFile) {
        Object object = parse(inputFile);
        if (object instanceof KCList) {
            return (KCList)object;
        } else {
            return null;
        }
    }

    private static KCMap toMap(JSONObject object) {
        Map<String, Object> map = new LinkedHashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.opt(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return new KCMap(map);
    }

    private static KCList toList(JSONArray array) {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.opt(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return new KCList(list);
    }

    private static boolean checkFileHeader(byte[] file, byte[] header) {
        if (file.length < header.length) {
            return false;
        }

        for (int i = 0; i < header.length; i++) {
            if (file[i] != header[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readStreamAsBytes(InputStream inputStream) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        } catch (IOException e) {
            return null;
        }

        return result.toByteArray();
    }

    private static String readStreamAsString(InputStream inputStream) {
        byte[] bytes = readStreamAsBytes(inputStream);

        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String decryptBytes(byte[] input, int offset) {
        char[] key = new char[]{'1', 'w', 'w', 'W', '6', '\\', 'G', 'F', '1', 'x', '$', '4', 'ä', 'Ù', '³', 'À', '1', 'R', '<', ']', '¡', '\u008a', 'Ð', '$', 'v', 'F', '\u009d', '®', 'v', '*', 'ê', '¦', 'C'};

        for(int i = 0; i < 32; ++i) {
            key[i] = (char)(key[i] ^ i * i % 255);
        }

        String sKey = String.valueOf(key, 0, 16);
        byte[] buffer = new byte[1024];

        try {
            SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] finalBytes = cipher.doFinal(input, offset, input.length - offset);
            String result = new String(finalBytes, "UTF-8");
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}

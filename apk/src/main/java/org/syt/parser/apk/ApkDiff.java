package org.syt.parser.apk;

import org.syt.parser.apk.bean.*;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.entry.BaseBean;
import org.syt.parser.json.JSONArray;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * zhulei 2025/3/11-20:20
 * apk 比较器
 */
public class ApkDiff {

    /**
     * 比较两个apk 支持 apk | xapk | apks | zip
     *
     * @param apk1 1
     * @param apk2 2
     * @return 比较结果
     */
    public static JSONObject diff(String apk1, String apk2) {
        return diff(apk1, apk2, false);
    }

    /**
     * 比较两个apk 支持 apk | xapk | apks | zip
     *
     * @param apk1      1
     * @param apk2      2
     * @param isDetails 是否详细模式
     * @return 比较结果
     */
    public static JSONObject diff(String apk1, String apk2, boolean isDetails) {
        try {
            AbstractApkFile apkFile1;
            AbstractApkFile apkFile2;
            if (apk1.endsWith(".apk")) {
                apkFile1 = new ApkFile(apk1);
            } else {
                apkFile1 = new ApksFile(apk1);
            }
            if (apk2.endsWith(".apk")) {
                apkFile2 = new ApkFile(apk2);
            } else {
                apkFile2 = new ApksFile(apk2);
            }

            if (isDetails) {
                return diff(apkFile1, apkFile2);
            } else {
                // 简单对比
                return compareApkAdjective(apkFile1, apkFile2);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 比较包体信息
     *
     * @param apkFile1 ApkMeta
     * @param apkFile2 ApkMeta
     * @return 简单对比结果
     */
    public static JSONObject compareApkAdjective(AbstractApkFile apkFile1, AbstractApkFile apkFile2) throws IOException, CertificateException, JSONException {
        if (apkFile1 == null || apkFile2 == null) {
            return null;
        }
        JSONObject diffs = new JSONObject();
        ApkMeta apkMeta1 = apkFile1.getApkMeta();
        ApkMeta apkMeta2 = apkFile2.getApkMeta();
        // 比较 包名
        String packageName1 = apkMeta1.getPackageName();
        String packageName2 = apkMeta2.getPackageName();
        if (!packageName1.equals(packageName2)) {
            diffs.putOpt("packageName", packageName1 + " vs " + packageName2);
            diffs.putOpt("scope", -1);
            return diffs;
        }

        // 比较应用名称
        String label1 = apkMeta1.getLabel();
        String label2 = apkMeta2.getLabel();
        if (!label1.equals(label2)) {
            diffs.putOpt("label", label1 + " vs " + label2);
        }

        // 比较四大组件 是否缺失
        diffs.putOpt("activity", compare(apkMeta1.getActivities(), apkMeta2.getActivities()));
        diffs.putOpt("service", compare(apkMeta1.getServices(), apkMeta2.getServices()));
        diffs.putOpt("receiver", compare(apkMeta1.getReceivers(), apkMeta2.getReceivers()));

        // 比较 签名
        List<CertificateMeta> certificateMetas1 = new ArrayList<>();
        List<CertificateMeta> certificateMetas2 = new ArrayList<>();

        for (ApkSigner apkSinger : apkFile1.getApkSingers()) {
            certificateMetas1.addAll(apkSinger.getCertificateMetas());
        }
        for (ApkV2Signer apkV2Signer : apkFile1.getApkV2Singers()) {
            certificateMetas1.addAll(apkV2Signer.getCertificateMetas());
        }

        for (ApkSigner apkSinger : apkFile2.getApkSingers()) {
            certificateMetas2.addAll(apkSinger.getCertificateMetas());
        }
        for (ApkV2Signer apkV2Signer : apkFile2.getApkV2Singers()) {
            certificateMetas2.addAll(apkV2Signer.getCertificateMetas());
        }

        JSONObject compare = compare(certificateMetas1, certificateMetas2);
        if (compare != null && !compare.isEmpty()) {
            diffs.putOpt("certificate", compare);
            diffs.putOpt("scope", -1);
        }

        // 判断 so 是否 缺失
        List<NativeLibs> nativeLibs = apkFile1.getNativeLibs();
        List<NativeLibs> nativeLibs2 = apkFile2.getNativeLibs();
        JSONObject navJson = NativeLibs.compare(nativeLibs, nativeLibs2);
        diffs.putOpt("so", navJson);
        // navJson - arm64-v8a 就不可信 应用会出现这个 问题
        if (navJson != null) {
            if (navJson.getJSONArray("-").contains("arm64-v8a")) {
                diffs.putOpt("scope", -1);
            }
        }

        // dex
        List<DexClassAll> dexClasses = apkFile1.getDexClassesAll();
        List<DexClassAll> dexClasses2 = apkFile2.getDexClassesAll();
        diffs.putOpt("dex", compare(dexClasses, dexClasses2));

        // 资源文件
        List<Res> resList1 = apkFile1.getResList();
        List<Res> resList2 = apkFile2.getResList();
        diffs.putOpt("res", compare(resList1, resList2));

        // assets
        List<Assets> assetsList1 = apkFile1.getAssetsList();
        List<Assets> assetsList2 = apkFile2.getAssetsList();
        diffs.putOpt("assets", compare(assetsList1, assetsList2));

        return diffs;
    }

    /**
     * 判断组件是否
     *
     * @param componentLists1
     * @param componentLists2
     */
    public static JSONObject compare(List<? extends BaseBean> componentLists1, List<? extends BaseBean> componentLists2) throws JSONException {
        Set<String> little = new HashSet<>();

        for (BaseBean component1 : componentLists1) {
            boolean con = false;
            for (BaseBean component2 : componentLists2) {
                if (component1.getFileName().equals(component2.getFileName())) {
                    con = true;
                    break;
                }
            }
            if (!con) {
                little.add(component1.getFileName());
            }
        }

        Set<String> more = new HashSet<>();
        for (BaseBean component2 : componentLists2) {
            boolean con = false;
            for (BaseBean component1 : componentLists1) {
                if (component1.getFileName().equals(component2.getFileName())) {
                    con = true;
                    break;
                }
            }
            if (!con) {
                more.add(component2.getFileName());
            }
        }

        JSONObject diffs = new JSONObject();
        if (!little.isEmpty()) {
            diffs.putOpt("-", little);
        }
        if (!more.isEmpty()) {
            diffs.putOpt("+", more);
        }

        // 对比其它参数 note
        for (BaseBean component2 : componentLists2) {
            for (BaseBean component1 : componentLists1) {
                if (component1.getFileName().equals(component2.getFileName())) {
                    if (!component1.equals(component2)) {
                        diffs.putOpt(component1.getFileName(), component1 + " -> " + component2);
                    }
                }
            }

        }

        if (diffs.isEmpty()) {
            return null;
        }
        return diffs;
    }

    /**
     * 比较两个apk信息
     *
     * @param apkFile1 1
     * @param apkFile2 2
     * @return 比较结果
     */
    private static JSONObject diff(AbstractApkFile apkFile1, AbstractApkFile apkFile2) throws IOException, JSONException {

        apkFile1.parseAll();
        apkFile2.parseAll();

        JSONObject json1 = apkFile1.toJson();
        JSONObject json2 = apkFile2.toJson();
        return (JSONObject) compareJSON(json1, json2, "");
    }

    public static Object compareJSON(Object obj1, Object obj2, String path) throws JSONException {
        System.out.println("path = " + path);
        // note 太慢了 内存占用过大 class 对比过慢
        JSONObject diffs = new JSONObject();

        if (obj1 instanceof JSONObject && obj2 instanceof JSONObject) {
            JSONObject json1 = (JSONObject) obj1;
            JSONObject json2 = (JSONObject) obj2;

            // 比较键集合
            Set<String> keys1 = json1.keySet();
            Set<String> keys2 = json2.keySet();
            Set<String> little = new HashSet<>();
            Set<String> more = new HashSet<>();
            for (String key : keys1) {
                if (!json2.containsKey(key)) {
                    little.add(key);
                }
            }
            for (String key : keys2) {
                if (!json1.containsKey(key)) {
                    more.add(key);
                }
            }
            if (!little.isEmpty()) {
                diffs.putOpt("-", little);
            }
            if (!more.isEmpty()) {
                diffs.putOpt("+", more);
            }

            // 递归比较共同键的值
            for (String key : keys1) {
                Object value1 = json1.get(key);
                if (json2.containsKey(key)) {
                    Object value2 = json2.get(key);
                    diffs.putOpt(key, compareJSON(value1, value2, path + "." + key));
                }
            }

        } else if (obj1 instanceof JSONArray && obj2 instanceof JSONArray) {
            JSONArray arr1 = (JSONArray) obj1;
            JSONArray arr2 = (JSONArray) obj2;
            if (arr1.isEmpty() && arr2.isEmpty()) {
                return null;
            }
            if (arr1.isEmpty()) {
                diffs.putOpt("+", arr2);
            } else if (arr2.isEmpty()) {
                diffs.putOpt("-", arr1);
            } else {
                JSONArray diffsList = new JSONArray();
                Object o1 = arr1.get(0);
                Object o2 = arr2.get(0);
                if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
                    // 通过 name path 判断是否相同
                    for (int i = 0; i < arr1.length(); i++) {
                        for (int j = 0; j < arr2.length(); j++) {
                            JSONObject value1 = (JSONObject) arr1.get(i);
                            JSONObject value2 = (JSONObject) arr2.get(j);
                            String name1 = value1.containsKey("name") ? value1.getString("name") : value1.getString("path");
                            String name2 = value1.containsKey("name") ? value1.getString("name") : value1.getString("path");
                            if (name1 == null || name2 == null) {
                                diffsList.put(compareJSON(arr1.get(i), arr2.get(j), path + "[" + i + "]."));
                            } else {
                                if (name1.equals(name2)) {
                                    diffsList.put(compareJSON(value1, value2, path + "." + name1));
                                }
                            }
                        }
                    }
                } else {
                    // 判断缺失，增多内容
                    if (arr1.length() != arr2.length()) {
                        diffs.putOpt("数组长度不一致", arr1.length() + " vs " + arr2.length());
                    } else {
                        for (int i = 0; i < arr1.length(); i++) {
                            diffsList.put(compareJSON(arr1.get(i), arr2.get(i), path + "[" + i + "]."));
                        }
                    }
                }
                if (!diffsList.isEmpty()) {
                    diffs.putOpt(path, diffsList);
                }
            }
        } else {
            // 基本类型比较
            if (!obj1.equals(obj2)) {
                return obj1 + " -> " + obj2;
            }
            return null;
        }
        if (diffs.isEmpty()) {
            return null;
        }
        return diffs;
    }


}

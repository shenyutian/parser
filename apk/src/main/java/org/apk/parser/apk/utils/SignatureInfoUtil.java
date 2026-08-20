package org.apk.parser.apk.utils;

import org.apk.parser.json.JSONArray;
import org.apk.parser.json.JSONException;
import org.apk.parser.json.JSONObject;
import org.apk.parser.util.MD5;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/**
 * 签名信息的公共转换工具。
 */
public final class SignatureInfoUtil {

    private SignatureInfoUtil() {
    }

    /**
     * 将首张签名证书转换为历史接口所需字段。
     */
    public static void appendCertificateInfo(JSONObject jsonObject, byte[] certificateData) throws Exception {
        Certificate certificate = CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certificateData));
        PublicKey publicKey = certificate.getPublicKey();
        Base64.Encoder encoder = Base64.getEncoder();

        jsonObject.putOpt("sign", MD5.toHexString(certificateData).replace("\n", ""));
        jsonObject.putOpt("SHA1", encoder.encodeToString(MD5.toSha1(publicKey.getEncoded())));
        jsonObject.putOpt("SHA256", encoder.encodeToString(MD5.toSha256(publicKey.getEncoded())));
        jsonObject.putOpt("fbkey", encoder.encodeToString(MD5.toSha1(certificate.getEncoded())));
    }

    /**
     * 将详细签名解析结果转换为结构化 JSON。
     */
    public static JSONObject toJson(ApkSignatureUtil.ApkSignatureResult result) throws JSONException {
        JSONObject json = new JSONObject();
        json.putOpt("apkLength", result.apkLength);
        json.putOpt("apkMd5", result.apkMd5);
        json.putOpt("apkSha256", result.apkSha256);

        JSONArray signatures = new JSONArray();
        for (ApkSignatureUtil.SignatureInfo info : result.signatures) {
            JSONObject item = new JSONObject();
            item.putOpt("scheme", info.scheme);
            item.putOpt("principal", info.principal);
            item.putOpt("issuer", info.issuer);
            item.putOpt("signature", info.signature);
            signatures.put(item);
        }
        json.putOpt("signatures", signatures);
        return json;
    }
}

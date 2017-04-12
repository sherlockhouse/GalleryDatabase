package com.freeme.community.manager;


import android.util.Log;

import com.freeme.community.utils.LogUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpOperation {
    private static final String     TAG        = "HttpOperation";
    private static       HttpClient httpClient = null;

    public static String postRequest(String url, Map<String, String> rawParams) {
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (String key : rawParams.keySet()) {
                params.add(new BasicNameValuePair(key, rawParams.get(key)));
            }

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(urlEncodedFormEntity);
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);

            HttpResponse httpResponse = httpClient.execute(post);
            LogUtil.i(TAG, "request resultCode : " + httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    public static String getRequest(String url) throws Exception {
        httpClient = getNewHttpClient();//new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        LogUtil.i(TAG, "getRequest ");
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(get);
            LogUtil.i(TAG, "httpResponse " + httpResponse);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                LogUtil.i(TAG, "result " + result);
                return result;
            } else {
                LogUtil.i(TAG, "getStatusCode != SC_OK " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return null;
    }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            // Set the default socket timeout (SO_TIMEOUT) // in
            // milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            HttpClient client = new DefaultHttpClient(ccm, params);
            return client;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static String postRequestAvatar(String url, Map<String, String> rawParams) {
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (String key : rawParams.keySet()) {
                params.add(new BasicNameValuePair(key, rawParams.get(key)));
            }

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(urlEncodedFormEntity);
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);

            HttpResponse httpResponse = httpClient.execute(post);
            LogUtil.i(TAG, "request resultCode : " + httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    public static int uploadFile(File file, String RequestURL) {
        String CHARSET = "utf-8";
        int res = 0;
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString(); // �߽��ʶ ������
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // ��������

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(10 * 1000);
            conn.setDoInput(true); // ����������
            conn.setDoOutput(true); // ���������
            conn.setUseCaches(false); // ������ʹ�û���
            conn.setRequestMethod("POST"); // ����ʽ
            conn.setRequestProperty("Charset", CHARSET); // ���ñ���
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            if (file != null) {
                /**
                 * ���ļ���Ϊ��ʱִ���ϴ�
                 */
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * �����ص�ע�⣺ name�����ֵΪ����������Ҫkey ֻ�����key �ſ��Եõ���Ӧ���ļ�
                 * filename���ļ������֣����׺��
                 */

                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * ��ȡ��Ӧ�� 200=�ɹ� ����Ӧ�ɹ�����ȡ��Ӧ����
                 */
                res = conn.getResponseCode();
                Log.e(TAG, "response code:" + res);
                if (res == 200) {
                    Log.e(TAG, "request success");
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                    Log.e(TAG, "result : " + result);
                } else {
                    Log.e(TAG, "request error");
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static class MySSLSocketFactory extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");


        public MySSLSocketFactory(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }


                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }


                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host,
                    port, autoClose);
        }
    }

}

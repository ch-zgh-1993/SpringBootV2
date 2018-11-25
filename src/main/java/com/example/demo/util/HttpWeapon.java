/*
* @Author: Zhang Guohua
* @Date:   2018-11-25 11:52:53
* @Last Modified by:   zgh
* @Last Modified time: 2018-11-25 11:52:57
* @Description: create by zgh
* @GitHub: Savour Humor
*/
package com.wsn.essearch.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a easy using http request tool based on Apache HttpClient and FastJson. Enjoy yourself! (Someone loves RestTemplate so much, but I don't buy it.)
 * Created by PokerZeus on 2017/7/8.
 * V1.3
 */

/*
    PZ Maven dependencies:
        <dependencies>

            <!--https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient -->
            <dependency>
                <groupId>org.apache.httpcomponents</groupId>
                <artifactId>httpclient</artifactId>
                <version>4.5.3</version>
            </dependency>

            <!--https://mvnrepository.com/artifact/org.apache.httpcomponents/httpmime -->
            <dependency>
                <groupId>org.apache.httpcomponents</groupId>
                <artifactId>httpmime</artifactId>
                <version>4.5.3</version>
            </dependency>

            <!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>1.2.35</version>
            </dependency>

        </dependencies>

    PZ Default request:
        Http Type:       HTTP
        Certificates:    Not Ignore
        Request Type:    GET
        Request URL:     http://66.112.213.234:9539
        Encode Charset:  UTF-8
        Decode Charset:  UTF-8
        Socket Timeout:  15s
        Connect Timeout: 15s
        Request Timeout: 60s

    PZ Using instruction:
        try {
            JSONObject response =
                HttpWeapon.prepareRequest([HTTP_TYPE])                  pz v1.0 
                        .setIgnoreCertificates([INGNORE_SSK])           pz v1.2
                        .setRequestType([REQUEST_TYPE])                 pz v1.0
                        .setBaseUrl([BASE_URL])                         pz v1.0
                        .addHeader([HEADER_KEY], [HEADER_VALUE])        pz v1.0
                        .addQueryParam([PARAM_KEY], [PARAM_VALUE])      pz v1.0
                        .addEntityParam([PARAM_KEY], [PARAM_VALUE])     pz v1.0
                        .addEntityForm([FORM_KEY], [FORM_DATA])         pz v1.0
                        .setEntity([SOME_JSON_DATA])                    pz v1.0
                        .setEncodeCharset([CHARSET_NAME])               pz v1.0
                        .setDecodeCharset([CHARSET_NAME])               pz v1.0
                        .execute()                                      pz v1.1
                        .getEntityJsonObjectThenClose();                pz v1.0
                        //.getEntityBytesThenClose();                   pz v1.3

            // Handle your response next!

        } catch (IOException e) {
            e.printStackTrace();
        }

    PZ Logging explain:
        For now, you should use Log4j instead of Log4j2 to show HttpClient's logs. Log4j2 is not supported officially yet, but will be supported in HttpClient 5.0.
        However, if you are using SpringBoot, please add 'logging.level.org.apache.http=[LOG_LEVEL]' to your 'application.properties' and set it to a property logging level.
*/

public class HttpWeapon {

    private HttpType httpType = HttpType.HTTP;
    private boolean ignoreCertificates = false;
    private RequestType requestType = RequestType.GET;
    private String baseUrl = "http://66.112.213.234:9539";
    private Map<String, String> headersRaw = null;
    private List<NameValuePair> queryParamsRaw = null;
    private List<NameValuePair> entityParamsRaw = null;
    private MultipartEntityBuilder entityFormBuilder = null;
    private HttpEntity entity = null;
    private Charset encodeCharset = Charset.forName("UTF-8");
    private Charset decodeCharset = Charset.forName("UTF-8");
    private int socketTimeout = 15000;
    private int connectTimeout = 15000;
    private int connectionRequestTimeout = 60000;

    public void setHttpType(HttpType httpType) {
        this.httpType = httpType;
    }

    public void setIgnoreCertificates(boolean ignoreCertificates) {
        this.ignoreCertificates = ignoreCertificates;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public void addHeader(String key, String value) {
        if (null == headersRaw)
            headersRaw = new HashMap<String, String>();
        headersRaw.put(key, value);
    }

    public void addQueryParam(String key, String value) {
        if (null == queryParamsRaw)
            queryParamsRaw = new ArrayList<NameValuePair>();
        queryParamsRaw.add(new BasicNameValuePair(key, value));
    }

    public void addEntityParam(String key, String value) {
        if (null == entityParamsRaw)
            entityParamsRaw = new ArrayList<NameValuePair>();
        entityParamsRaw.add(new BasicNameValuePair(key, value));
    }

    public <E> void addFormElement(String key, E element) {
        if (null == entityFormBuilder)
            entityFormBuilder = MultipartEntityBuilder.create();
        if (element instanceof String) {
            entityFormBuilder.addPart(key, new StringBody((String) element, ContentType.TEXT_PLAIN.withCharset(encodeCharset)));
        } else if (element instanceof JSON) {
            entityFormBuilder.addPart(key, new StringBody(((JSON) element).toJSONString(), ContentType.APPLICATION_JSON.withCharset(encodeCharset)));
        } else if (element instanceof File) {
            entityFormBuilder.addPart(key, new FileBody((File) element));
        }
    }

    public <E> void setEntity(E entity) {
        if (entity instanceof String) {
            this.entity = new StringEntity((String) entity, ContentType.TEXT_PLAIN.withCharset(encodeCharset));
        } else if (entity instanceof JSON) {
            this.entity = new StringEntity(((JSON) entity).toJSONString(), ContentType.APPLICATION_JSON.withCharset(encodeCharset));
        } else if (entity instanceof File) {
            this.entity = new FileEntity((File) entity);
        }
    }

    public void setEncodeCharset(String charset) throws UnsupportedCharsetException {
        this.encodeCharset = Charset.forName(charset);
    }

    public void setDecodeCharset(String charset) throws UnsupportedCharsetException {
        this.decodeCharset = Charset.forName(charset);
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public static HttpWeaponBuilder prepareRequest() {
        return new HttpWeaponBuilder();
    }

    public static HttpWeaponBuilder prepareRequest(HttpType httpType) {
        return new HttpWeaponBuilder(httpType);
    }

    public HttpWeaponResponse execute() throws IOException {

        // PZ 最终裁定HTTP请求类型
        if (baseUrl.startsWith("http://"))
            httpType = HttpType.HTTP;
        else if (baseUrl.startsWith("https://"))
            httpType = HttpType.HTTPS;

        // PZ 构建URL参数体
        if (null != queryParamsRaw && queryParamsRaw.size() > 0) {
            baseUrl += "?" + URLEncodedUtils.format(queryParamsRaw, encodeCharset);
        }

        // PZ 构建BODY体
        if (null == this.entity) {
            if (null != entityParamsRaw && entityParamsRaw.size() > 0) {
                entity = new UrlEncodedFormEntity(entityParamsRaw, encodeCharset);
            } else if (null != entityFormBuilder) {
                entity = entityFormBuilder.build();
            }
        }

        // PZ 构建客户端
        CloseableHttpClient client = null;
        switch (httpType) {
            case HTTP:
                if (!baseUrl.startsWith("http://"))
                    baseUrl = "http://" + baseUrl;
                client = HttpClients.createDefault();
                break;
            case HTTPS:
                if (!baseUrl.startsWith("https://")) {
                    baseUrl = "https://" + baseUrl;
                }
                if (ignoreCertificates) {
                    try {
                        client = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContextBuilder.create().loadTrustMaterial((x509Certificates, s) -> true).build(), new DefaultHostnameVerifier())).build();
                    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                        e.printStackTrace();
                    }
                } else {
                    PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(new URL(baseUrl));
                    DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcher);
                    client = HttpClients.custom().setSSLHostnameVerifier(hostnameVerifier).build();
                }
                break;
        }

        // PZ 配置HTTP请求
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();


        // PZ 执行，返回RESPONSE
        CloseableHttpResponse response = null;

        switch (requestType) {
            case GET:
                HttpGet httpGet = new HttpGet(baseUrl);
                httpGet.setConfig(requestConfig);
                if (null != headersRaw && headersRaw.size() > 0) {
                    for (String key : headersRaw.keySet()) {
                        httpGet.addHeader(key, headersRaw.get(key));
                    }
                }
                response = client.execute(httpGet);
                break;
            case POST:
                HttpPost httpPost = new HttpPost(baseUrl);
                httpPost.setConfig(requestConfig);
                if (null != headersRaw && headersRaw.size() > 0) {
                    for (String key : headersRaw.keySet()) {
                        httpPost.addHeader(key, headersRaw.get(key));
                    }
                }
                if (null != entity)
                    httpPost.setEntity(entity);
                response = client.execute(httpPost);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(baseUrl);
                httpPut.setConfig(requestConfig);
                if (null != headersRaw && headersRaw.size() > 0) {
                    for (String key : headersRaw.keySet()) {
                        httpPut.addHeader(key, headersRaw.get(key));
                    }
                }
                if (null != entity)
                    httpPut.setEntity(entity);
                response = client.execute(httpPut);
                break;
            case DELETE:
                HttpDelete httpDelete = new HttpDelete(baseUrl);
                httpDelete.setConfig(requestConfig);
                if (null != headersRaw && headersRaw.size() > 0) {
                    for (String key : headersRaw.keySet()) {
                        httpDelete.addHeader(key, headersRaw.get(key));
                    }
                }
                response = client.execute(httpDelete);
                break;
        }

        return new HttpWeaponResponse(client, response, decodeCharset);

    }

    /**
     * Response
     */
    public static class HttpWeaponResponse {
        private CloseableHttpResponse response = null;
        private CloseableHttpClient client = null;
        private Charset decodeCharset = Charset.forName("UTF-8");

        public HttpWeaponResponse(CloseableHttpClient client, CloseableHttpResponse response, Charset decodeCharset) {
            this.client = client;
            this.response = response;
            this.decodeCharset = decodeCharset;
        }

        public HttpWeaponResponse() {
        }

        protected void setClient(CloseableHttpClient client) {
            this.client = client;
        }

        protected void setResponse(CloseableHttpResponse response) {
            this.response = response;
        }

        protected void setDecodeCharset(Charset decodeCharset) {
            this.decodeCharset = decodeCharset;
        }

        public int getStatusCode() {
            return response.getStatusLine().getStatusCode();
        }

        public String getStatusReason() {
            return response.getStatusLine().getReasonPhrase();
        }

        public Header[] getHeaders() {
            return response.getAllHeaders();
        }

        public String getEntityString(String charset) throws IOException {
            return EntityUtils.toString(response.getEntity(), charset);
        }

        public String getEntityString() throws IOException {
            return EntityUtils.toString(response.getEntity(), decodeCharset);
        }

        public String getEntityStringThenClose(String charset) throws IOException {
            String ret = EntityUtils.toString(response.getEntity(), charset);
            close();
            return ret;
        }

        public String getEntityStringThenClose() throws IOException {
            String ret = EntityUtils.toString(response.getEntity(), decodeCharset);
            close();
            return ret;
        }

        @Deprecated
        public JSONObject getEntityJsonObject() throws IOException, JSONException {
            return JSON.parseObject(EntityUtils.toString(response.getEntity(), decodeCharset), Feature.OrderedField);
        }

        @Deprecated
        public JSONArray getEntityJsonArray() throws IOException, JSONException {
            return JSON.parseArray(EntityUtils.toString(response.getEntity(), decodeCharset));
        }

        public JSONObject getEntityJsonObjectThenClose() throws IOException, JSONException {
            JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(response.getEntity(), decodeCharset), Feature.OrderedField);
            close();
            return jsonObject;
        }

        public JSONArray getEntityJsonArrayThenClose() throws IOException, JSONException {
            JSONArray jsonArray = JSON.parseArray(EntityUtils.toString(response.getEntity(), decodeCharset));
            close();
            return jsonArray;
        }

        public byte[] getEntityBytesThenClose() throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(outputStream);
            return outputStream.toByteArray();
        }

        public void close() throws IOException {
            response.close();
            client.close();
        }

    }

    /**
     * Builder
     */
    public static class HttpWeaponBuilder {

        private HttpWeapon request;

        public HttpWeaponBuilder() {
            request = new HttpWeapon();
        }

        public HttpWeaponBuilder(HttpType httpType) {
            request = new HttpWeapon();
            request.setHttpType(httpType);
        }

        public HttpWeaponBuilder setHttpType(HttpType httpType) {
            request.setHttpType(httpType);
            return this;
        }

        public HttpWeaponBuilder setIgnoreCertificates(boolean ignoreCertificates) {
            request.setIgnoreCertificates(ignoreCertificates);
            return this;
        }

        public HttpWeaponBuilder setRequestType(RequestType requestType) {
            request.setRequestType(requestType);
            return this;
        }

        public HttpWeaponBuilder setBaseUrl(String baseUrl) {
            request.setBaseUrl(baseUrl);
            return this;
        }

        public HttpWeaponBuilder addHeader(String key, String value) {
            request.addHeader(key, value);
            return this;
        }

        public HttpWeaponBuilder addQueryParam(String key, String value) {
            request.addQueryParam(key, value);
            return this;
        }

        public HttpWeaponBuilder addEntityParam(String ket, String value) {
            request.addEntityParam(ket, value);
            return this;
        }

        public <E> HttpWeaponBuilder addFormElement(String key, E element) {
            request.addFormElement(key, element);
            return this;
        }

        public <E> HttpWeaponBuilder setEntity(E entity) {
            request.setEntity(entity);
            return this;
        }

        public HttpWeaponBuilder setEncodingCharset(String charset) {
            request.setEncodeCharset(charset);
            return this;
        }

        public HttpWeaponBuilder setDecodingCharset(String charset) {
            request.setDecodeCharset(charset);
            return this;
        }

        public HttpWeaponBuilder setSocketTimeout(int socketTimeout) {
            request.setSocketTimeout(socketTimeout);
            return this;
        }

        public HttpWeaponBuilder setConnectTimeout(int socketTimeout) {
            request.setConnectTimeout(socketTimeout);
            return this;
        }

        public HttpWeaponBuilder setConnectionRequestTimeout(int socketTimeout) {
            request.setConnectionRequestTimeout(socketTimeout);
            return this;
        }

        public HttpWeaponResponse execute() throws IOException {
            return request.execute();
        }

    }

    public enum HttpType {
        HTTP,
        HTTPS
    }

    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }
}

/*
    PZ v1.0 2017-7-20
    完成初版功能开发

    PZ v1.1 2017-8-11
    增加最终裁定HttpType:以BaseUrl为主
    修正拼写错误

    PZ v1.2 2017-8-15
    增加忽略HTTPS时SSl证书

    PZ v1.3 2018-1-25
    增加返回体为比特流型
*/
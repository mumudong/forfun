package encode;

public class ResponseSample {
    private String code;
    private String data;
    private Long timestamp;

    public ResponseSample() {
    }

    public ResponseSample(String code, String data, Long timestamp) {
        this.code = code;
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ResponseSample{" +
                "code='" + code + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

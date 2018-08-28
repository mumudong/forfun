package common;

import java.io.Serializable;
import java.util.Date;

/**
 *  Created by MuDong on 2017/8/10.
 */
public class BaiduNewsResult  implements Serializable{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Date createtime;
    private String title;
    private String url;
    private String source;
    private String time;
    private String intro;
    private String label;
    private String content;
    private String website;
    private String keyword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }


    public String getTime() {
        return time;
    }


    public void setTime(String time) {
        this.time = time;
    }


    public String getIntro() {
        return intro;
    }


    public void setIntro(String intro) {
        this.intro = intro;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public String getWebsite() {
        return website;
    }


    public void setWebsite(String website) {
        this.website = website;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return "BaiduNewsResult{" +
                "id=" + id +
                ", createtime=" + createtime +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", source='" + source + '\'' +
                ", time='" + time + '\'' +
                ", intro='" + intro + '\'' +
                ", label='" + label + '\'' +
                ", content='" + content + '\'' +
                ", website='" + website + '\'' +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}

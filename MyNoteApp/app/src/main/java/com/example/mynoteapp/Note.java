package com.example.mynoteapp;

public class
Note {
    private String id;
    private String title;
    private String content;

    public Note() {
        // 空的默认构造函数，Firebase 需要使用
    }

    public Note(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

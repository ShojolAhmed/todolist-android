package com.shojolahmed.todolist.Model;

import java.util.Date;

public class TodoModel extends TaskId {

    private String task_name, date_created, description;
    private int status, has_description;
    private Date time;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHas_description() {
        return has_description;
    }

    public void setHas_description(int has_description) {
        this.has_description = has_description;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}

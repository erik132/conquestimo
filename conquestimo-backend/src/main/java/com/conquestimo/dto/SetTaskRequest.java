package com.conquestimo.dto;

import jakarta.validation.constraints.NotNull;

public class SetTaskRequest {
    @NotNull
    private String task;
    private String constructionTarget;

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getConstructionTarget() { return constructionTarget; }
    public void setConstructionTarget(String constructionTarget) { this.constructionTarget = constructionTarget; }
}

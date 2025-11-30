package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    private String startPoint;
    private boolean checkout;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public boolean isCheckout() {
        return checkout;
    }

    public void setCheckout(boolean checkout) {
        this.checkout = checkout;
    }
}

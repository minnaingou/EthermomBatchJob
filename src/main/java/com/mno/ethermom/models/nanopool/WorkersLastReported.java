package com.mno.ethermom.models.nanopool;

import lombok.Data;

import java.util.List;

@Data
public class WorkersLastReported {

    private boolean status;
    private List<WorkerStats> data;

}
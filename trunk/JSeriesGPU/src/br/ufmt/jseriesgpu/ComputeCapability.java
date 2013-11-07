/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jseriesgpu;

/**
 *
 * @author raphael
 */
 public class ComputeCapability {

    private String computeCapability;
    private String sMVersion;
    private int threadsWarp;
    private int warpsMultiprocessor;
    private int threadsMultiprocessor;
    private int threadBlocksMultiprocessor;
    private int maxSharedMemoryMultiprocessorBytes;
    private int registerFileSize;
    private int registerAllocationUnitSize;
    private String allocationGranularity;
    private int maxRegistersThread;
    private int sharedMemoryAllocationUnitSize;
    private int warpAllocationGranularity;
    private int maxThreadBlockSize;
    private int sharedMemorySizeConfigurationsBytes;

    public ComputeCapability() {
    }

    public ComputeCapability(String computeCapability, String sMVersion, int threadsWarp, int warpsMultiprocessor, int threadsMultiprocessor, int threadBlocksMultiprocessor, int maxSharedMemoryMultiprocessorBytes, int registerFileSize, int registerAllocationUnitSize, String allocationGranularity, int maxRegistersThread, int sharedMemoryAllocationUnitSize, int warpallocationgranularity, int maxThreadBlockSize, int sharedMemorySizeConfigurationsBytes) {
        this.computeCapability = computeCapability;
        this.sMVersion = sMVersion;
        this.threadsWarp = threadsWarp;
        this.warpsMultiprocessor = warpsMultiprocessor;
        this.threadsMultiprocessor = threadsMultiprocessor;
        this.threadBlocksMultiprocessor = threadBlocksMultiprocessor;
        this.maxSharedMemoryMultiprocessorBytes = maxSharedMemoryMultiprocessorBytes;
        this.registerFileSize = registerFileSize;
        this.registerAllocationUnitSize = registerAllocationUnitSize;
        this.allocationGranularity = allocationGranularity;
        this.maxRegistersThread = maxRegistersThread;
        this.sharedMemoryAllocationUnitSize = sharedMemoryAllocationUnitSize;
        this.warpAllocationGranularity = warpallocationgranularity;
        this.maxThreadBlockSize = maxThreadBlockSize;
        this.sharedMemorySizeConfigurationsBytes = sharedMemorySizeConfigurationsBytes;
    }

    public String getAllocationGranularity() {
        return allocationGranularity;
    }

    public void setAllocationGranularity(String allocationGranularity) {
        this.allocationGranularity = allocationGranularity;
    }

    public String getComputeCapability() {
        return computeCapability;
    }

    public void setComputeCapability(String computeCapability) {
        this.computeCapability = computeCapability;
    }

    public int getMaxRegistersThread() {
        return maxRegistersThread;
    }

    public void setMaxRegistersThread(int maxRegistersThread) {
        this.maxRegistersThread = maxRegistersThread;
    }

    public int getMaxSharedMemoryMultiprocessorBytes() {
        return maxSharedMemoryMultiprocessorBytes;
    }

    public void setMaxSharedMemoryMultiprocessorBytes(int maxSharedMemoryMultiprocessorBytes) {
        this.maxSharedMemoryMultiprocessorBytes = maxSharedMemoryMultiprocessorBytes;
    }

    public int getMaxThreadBlockSize() {
        return maxThreadBlockSize;
    }

    public void setMaxThreadBlockSize(int maxThreadBlockSize) {
        this.maxThreadBlockSize = maxThreadBlockSize;
    }

    public int getRegisterAllocationUnitSize() {
        return registerAllocationUnitSize;
    }

    public void setRegisterAllocationUnitSize(int registerAllocationUnitSize) {
        this.registerAllocationUnitSize = registerAllocationUnitSize;
    }

    public int getRegisterFileSize() {
        return registerFileSize;
    }

    public void setRegisterFileSize(int registerFileSize) {
        this.registerFileSize = registerFileSize;
    }

    public String getSMVersion() {
        return sMVersion;
    }

    public void setSMVersion(String sMVersion) {
        this.sMVersion = sMVersion;
    }

    public int getSharedMemoryAllocationUnitSize() {
        return sharedMemoryAllocationUnitSize;
    }

    public void setSharedMemoryAllocationUnitSize(int sharedMemoryAllocationUnitSize) {
        this.sharedMemoryAllocationUnitSize = sharedMemoryAllocationUnitSize;
    }

    public int getSharedMemorySizeConfigurationsBytes() {
        return sharedMemorySizeConfigurationsBytes;
    }

    public void setSharedMemorySizeConfigurationsBytes(int sharedMemorySizeConfigurationsBytes) {
        this.sharedMemorySizeConfigurationsBytes = sharedMemorySizeConfigurationsBytes;
    }

    public int getThreadBlocksMultiprocessor() {
        return threadBlocksMultiprocessor;
    }

    public void setThreadBlocksMultiprocessor(int threadBlocksMultiprocessor) {
        this.threadBlocksMultiprocessor = threadBlocksMultiprocessor;
    }

    public int getThreadsMultiprocessor() {
        return threadsMultiprocessor;
    }

    public void setThreadsMultiprocessor(int threadsMultiprocessor) {
        this.threadsMultiprocessor = threadsMultiprocessor;
    }

    public int getThreadsWarp() {
        return threadsWarp;
    }

    public void setThreadsWarp(int threadsWarp) {
        this.threadsWarp = threadsWarp;
    }

    public int getWarpAllocationGranularity() {
        return warpAllocationGranularity;
    }

    public void setWarpAllocationGranularity(int warpallocationgranularity) {
        this.warpAllocationGranularity = warpallocationgranularity;
    }

    public int getWarpsMultiprocessor() {
        return warpsMultiprocessor;
    }

    public void setWarpsMultiprocessor(int warpsMultiprocessor) {
        this.warpsMultiprocessor = warpsMultiprocessor;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufmt.genericlexerseb;

/**
 *
 * @author raphael
 */
public class Structure {
    
    private String token;
    private boolean vector;
    private String index;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isVector() {
        return vector;
    }

    public void setVector(boolean vector) {
        this.vector = vector;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
    
}

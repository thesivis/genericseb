/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.screen;

import br.ufmt.util.GeneSEBean;
import javax.swing.JPanel;

/**
 *
 * @author raphael
 */
public abstract class ScreenAbstract extends JPanel {

    public abstract String[] getExtensions();

    public abstract String getSaveExtension();

    public abstract void setBean(GeneSEBean bean);

    public abstract GeneSEBean getBean();

}

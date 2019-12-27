/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namsor.oss.classify.bayes;

/**
 * Contains additional details on the classifications, such as the formulas or the algebraic calculation.
 * @author elian
 */
public interface IClassificationExplained extends IClassification {
    
    /**
     * Get the likelyhoods values, ex. 0.011806375442739082
     * @return 
     */
    double[] getLikelyhoods();
    
    /**
     * For each likelyhood, get the formula ex. 
     * gL_cA_Yes / gL * 
     * ((gL_cA_Yes_fE_temp_is_Cool + alpha)/(gL_cA_Yes_fE_temp + ( gL_fE_temp_count * alpha )) * 
     * (gL_cA_Yes_fE_humidity_is_High + alpha)/(gL_cA_Yes_fE_humidity + ( gL_fE_humidity_count * alpha )) * 
     * (gL_cA_Yes_fE_outlook_is_Overcast + alpha)/(gL_cA_Yes_fE_outlook + ( gL_fE_outlook_count * alpha )) * 
     * (gL_cA_Yes_fE_wind_is_Strong + alpha)/(gL_cA_Yes_fE_wind + ( gL_fE_wind_count * alpha )) * 1 )
     * @return The formula 
     */
    String[] getLikelyhoodFormulae();
    
    /**
     * For each likelyhood, get the expression ex. 
     * 9 / 14 * 
     * ((3 + 1.0 )/(9 + ( 3 * 1.0 )) * 
     * (3 + 1.0 )/(9 + ( 2 * 1.0 )) * 
     * (4 + 1.0 )/(9 + ( 3 * 1.0 )) * 
     * (3 + 1.0 )/(9 + ( 2 * 1.0 )) * 1 )
     * @return The algebraic expression
     */
    String[] getLikelyhoodExpressions();       
}
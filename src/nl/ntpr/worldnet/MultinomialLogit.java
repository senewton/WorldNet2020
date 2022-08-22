/*
 * MultinomialLogit.java
 */

package nl.ntpr.worldnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.nea.neac.worldnet.network.Path;

/**
 * MultinomialLogit attaches choice probabilities to paths connecting a given origin destination pair.
 * @author sne
 */
public final class MultinomialLogit
{
    // Do not allow anyone to instantiate this class.
    private MultinomialLogit(){
        // Do nothing
    }

    /**
     * Calculate runs the MNL function, assigning probabilities to each element in the set.  The multinomial logit is a
     * simple way of mapping probabilities (a set of numbers between 0 and 1 adding up to precisely 1) to the elements
     * in the choice set, based on their relative cost.  The beta parameter is fixed here, but could be an estimated
     * parameter.
     * @param paths a reference to a List of Paths.
     * @return Map containing the probability that a Path is chosen for each Path passed.
     */
    public static Map<Path, Double> calculate(List<Path> paths, double beta)
    {        
        double total = 0.0;
        double minImpedance = java.lang.Double.POSITIVE_INFINITY;
        
        int itemsInSet = paths.size();
        
        // Work out lowest cost route: choice set not guaranteed to be sorted
        for (Path value : paths) {
            if (value.getImpedance() < minImpedance) {
                minImpedance = value.getImpedance();
            }
        }

        // if ( minImpedance < 100.0 ){
        //    System.out.println("Min Impedance =" + minImpedance );
        // }

        // Calculate the denominator
        for (Path value : paths) {
            double indexedImpedance = 100.0 * (value.getImpedance() / minImpedance);
            total += Math.exp(beta * indexedImpedance);
        }

        HashMap<Path, Double> result = new HashMap<Path, Double>(itemsInSet);

        // Set the probabilities into the main data structure
        for (Path path : paths) {
            double indexedImpedance = 100.0 * (path.getImpedance() / minImpedance);
            double probability = (Math.exp(beta * indexedImpedance)) / total;
            result.put(path, probability);
        }
        return result;        
    }   
}

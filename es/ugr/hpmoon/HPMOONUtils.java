/*
 * Copyright (C) 2015 pgarcia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ugr.hpmoon;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.VectorIndividual;


/**
 *
 * @author pgarcia
 */
public class HPMOONUtils {
    
    public static final String P_DISJOINT = "disjoint";
    public static final String DISJOINT_TRUE = "true";
    public static final String DISJOINT_FALSE = "false";
    public static final String DISJOINT_NONE = "none";
    
    public static int[] getIslandIdAndNumIslands(EvolutionState state, int subpopulation, int thread){
        
        int[] info = new int[2];
         //Parameter paramIslandId = this.mybase.push("islandId"); (this would search ec.subpopulation.blahblah
        Parameter hpmoonNumIslands = new Parameter("hpmoon.num-islands");
        Parameter hpmoonIslandId = new Parameter("hpmoon.island-id");
        
        if(state.parameters.exists(hpmoonNumIslands, null)){
            //Exchanger 
            info[1] = state.parameters.getInt(hpmoonNumIslands, null); //number of islands
            info[0] = state.parameters.getInt(hpmoonIslandId, null); //islandId
            
            
        }else{
            Parameter exchanger = new Parameter("exch");
            String islandExc = state.parameters.getString(exchanger, null);
            if(islandExc.equals("ec.exchange.InterPopulationExchange") || islandExc.equals("es.ugr.hpmoon.InterPopulationRandomExchange")){
                info[0] = subpopulation;
                info[1] = state.population.subpops.length;
            }else{
                state.output.fatal("ERROR: Not number of islands specified for Island Exchanger, nor InternalIslandExchanger used");
            }
            
              
            
        }
        
        return info;
    
    }
    
        public static int[] getCutPoints(int numberOfIslands, int chunkSize){
        int[] cutpoints = new int[numberOfIslands-1];
            for(int i=0;i<numberOfIslands-1;i++)
                cutpoints[i] = (i+1)*chunkSize;
        return cutpoints;
    }
    
    public static VectorIndividual getSubIndividual(VectorIndividual ind, int islandId, int numberOfIslands, String disj){
        
        int chunkSize = ind.genomeLength()/numberOfIslands;
                       
        VectorIndividual chunk0 =  (VectorIndividual) ind.clone();
        int[] points = getCutPoints(numberOfIslands, chunkSize);
        

            int pre = (islandId-1)%numberOfIslands;
            if (pre<0) pre = pre+numberOfIslands;
            int pos = (islandId+1)%numberOfIslands;
        
            //Creating split points and cutting the individual in pieces

            Object[] chunks0 = new Object[numberOfIslands];
            ind.split(points, chunks0);

            
            
            if(disj.equals(DISJOINT_FALSE)){
               // state.output.message("NO SON DISJUNTOS "+pre+" " +islandId+" "+pos);
                Object[] forChunk0 = new Object[3];

                
                 forChunk0[0] = chunks0[pre];
                 forChunk0[1] = chunks0[islandId]; 
                 forChunk0[2] = chunks0[pos];

                
                chunk0.join(forChunk0);
                return chunk0;
                
                
                    
            }else{ 
                if(disj.equals(DISJOINT_TRUE)){
                    Object chunkGenome0 = chunks0[islandId]; 
                    chunk0.setGenome(chunkGenome0);
                    return chunk0;
                }else{
                    return chunk0;
                }
            }
    }
    
    public static void reconstructIndividual(VectorIndividual originalIndividual, VectorIndividual changedIndividual, String disj, int islandId, int numberOfIslands) {

        int chunkSize = originalIndividual.genomeLength() / numberOfIslands;
        int pre = (islandId - 1) % numberOfIslands;
        if (pre < 0) {
            pre = pre + numberOfIslands;
        }
        int pos = (islandId + 1) % numberOfIslands;
        
        int[] points = getCutPoints(numberOfIslands, chunkSize);

        int lastsize = originalIndividual.genomeLength() - points[points.length - 1];
        Object[] chunks0 = new Object[numberOfIslands];
        originalIndividual.split(points, chunks0);

        if (disj.equals(DISJOINT_FALSE)) {

            int[] newpoints = new int[2];
            newpoints[0] = chunkSize;
            newpoints[1] = chunkSize * 2;

            if (islandId == 0) {
                newpoints[0] = lastsize;
                newpoints[1] = lastsize + chunkSize;
            }
            if (islandId == (numberOfIslands - 1)) {
                newpoints[1] = chunkSize + lastsize;
            }

            Object[] chunks0_3 = new Object[3];
            changedIndividual.split(newpoints, chunks0_3);
            chunks0[pre] = chunks0_3[0];
            chunks0[islandId] = chunks0_3[1];
            chunks0[pos] = chunks0_3[2];
            originalIndividual.join(chunks0);

        } else {
            if (disj.equals(DISJOINT_TRUE)) {
                chunks0[islandId] = changedIndividual.getGenome();
                originalIndividual.join(chunks0);
            } else {
                originalIndividual.setGenome(changedIndividual.getGenome());
            }
        }


    }
    
}

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
package es.ugr.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.InvertedGenerationalDistance;
import jmetal.qualityIndicator.Spread;


/**
 *
 * @author pgarcia
 */
public class ShowStatistics {
    public static String JOB_ID = "job.";
    public static String STAT_EXTENSION = "stat";
    public static String COLUMNS_EXTENSION = "columns";

    
    
    public void showMOstatistics(String directory, String filedescriptor, int numJobs, double[][] truePareto) {
        Hypervolume hvcalculator = new Hypervolume();
        Spread spreadcalculator = new Spread();
        InvertedGenerationalDistance igdcalculator = new InvertedGenerationalDistance();

        double[] hvs = new double[numJobs];
        double[] spreads = new double[numJobs];
        double[] idcs = new double[numJobs];
        double[] avgnumsols = new double[numJobs];
        double[] times = new double[numJobs];

        System.out.println(filedescriptor);
        String columnsFile = directory + "/columns/" + filedescriptor + ".txt";
       
        PrintWriter writer, writerPareto;
        try {
            writer = new PrintWriter(columnsFile, "UTF-8");
            

            for (int i = 0; i < numJobs; i++) {
                String filename = directory + "/" + JOB_ID + i + "." + filedescriptor + "_front.stat";
                String filenameTime = directory + "/" + JOB_ID + i + "." + filedescriptor + "_salida.stat";
                double[][] pareto = extractParetoFromFile(filename, 2);
                if (pareto.length < 1) {
                    System.out.println("ERROR: PARETO <1");
                    return;
                }
                if(filedescriptor.equals("128_true_512_zdt1"))
                    System.out.println("PARA");
                double hv = hvcalculator.hypervolume(pareto, truePareto, 2);
                double hvtp = hvcalculator.hypervolume(truePareto, truePareto, 2);
                double spread = spreadcalculator.spread(pareto, truePareto, 2);
                double igd = igdcalculator.invertedGenerationalDistance(pareto, truePareto, 2);
                int numsol = pareto.length;
                double time = readTime(filenameTime);
                String toPrint = hv + "\t" + spread + "\t" + igd + "\t" + numsol + "\t" + time+"\n";
                System.out.println(toPrint);
                writer.write(toPrint);
                
                writerPareto = new PrintWriter(filename+"ONLYPARETO","UTF-8"); //SOBREESCSRIBEEEE! AGH!
                for(int j = 0; j<pareto.length;j++)
                    writerPareto.write(pareto[0]+" "+pareto[1]+"\n");
                writerPareto.close();

            }
            writer.close();
            
        } catch (Exception ex) {
            System.out.println("EXCEPTION CREATING FILE" + ex.getMessage());
        }
        System.out.println("AVERAGES ");

    }
    
    //TODO modify getParetoFromBidim to add more dimensions than 2 
    public double[][] extractParetoFromFile(String filename, int dimensions){
        ExtractParetoFront epf = new ExtractParetoFront(filename, dimensions);
        
        return epf.getParetoFrontBidim();
    }
    
    
    public double readTime(String filename){
        File f = new File(filename);
        String last = tail2(f,1);
        String[] splits = last.split(" |\n");
        int time = Integer.parseInt(splits[1]);
        return time;
    }
    
    public static void main(String [] args) {
        ShowStatistics ss = new ShowStatistics();
        String[] problems = {"zdt1","zdt2","zdt3","zdt6"};
        String[] disjoint = {"none","true","false"};
        int[] islands = {8, 32, 128};
        String[] dimensions = {"512","2048"};
        String dir = "/home/pgarcia/hpmoondata";
        //job.22.32_false_512_zdt2_front.stat
        for(String p:problems){
            String trueparetofile = dir+"/"+p+".truepareto";
            double[][] truepareto = ss.extractParetoFromFile(trueparetofile, 2);
            for(int is:islands)
                for(String disj:disjoint)
                    for(String dim:dimensions){
                        String filedescriptor = is+"_"+disj+"_"+dim+"_"+p;
                        ss.showMOstatistics(dir, filedescriptor , 30,truepareto);
                    }
                
        }
        
    }
    
    public String tail2( File file, int lines) {
    java.io.RandomAccessFile fileHandler = null;
    try {
        fileHandler = 
            new java.io.RandomAccessFile( file, "r" );
        long fileLength = fileHandler.length() - 1;
        StringBuilder sb = new StringBuilder();
        int line = 0;

        for(long filePointer = fileLength; filePointer != -1; filePointer--){
            fileHandler.seek( filePointer );
            int readByte = fileHandler.readByte();

             if( readByte == 0xA ) {
                if (filePointer < fileLength) {
                    line = line + 1;
                }
            } else if( readByte == 0xD ) {
                if (filePointer < fileLength-1) {
                    line = line + 1;
                }
            }
            if (line >= lines) {
                break;
            }
            sb.append( ( char ) readByte );
        }

        String lastLine = sb.reverse().toString();
        return lastLine;
    } catch( java.io.FileNotFoundException e ) {
        e.printStackTrace();
        return null;
    } catch( java.io.IOException e ) {
        e.printStackTrace();
        return null;
    }
    finally {
        if (fileHandler != null )
            try {
                fileHandler.close();
            } catch (IOException e) {
            }
    }
}
}
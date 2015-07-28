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
import ec.multiobjective.MultiObjectiveStatistics;
import ec.util.Parameter;

/**
 *
 * @author pgarcia
 */
public class HPMOONStatistics extends MultiObjectiveStatistics{
    @Override
    public void setup(final EvolutionState state, final Parameter base){
        super.setup(state,base);
    }
    
    public void finalStatistics(final EvolutionState state, final int result){
        bypassFinalStatistics(state, result);  // just call super.super.finalStatistics(...)

        if (doFinal){
            state.output.println("EXTRA STATISTICS", statisticslog);
            state.output.println("MEGAPARETO", statisticslog);
        }
    }
    
}
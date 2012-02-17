package ru.vasily.application.appstrategy;

import java.io.IOException;

import ru.vasily.core.io.FileSystem;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;
import ru.vasily.application.SolverFacade.IterativeSolver;

public class IterativeAppStrategy extends AbstractAppStrategy
{

    public IterativeAppStrategy(DataObjectService objService, SolverFacade solver,
                                IResultWriter dataWriter, FileSystem fileSystem)
    {
        super(objService, solver, dataWriter, fileSystem);
    }

    @Override
    public void processInputFile(String inputFile)
            throws IOException
    {
        IterativeSolver iterativeSolver = solver.getSolver(parseDataObject(inputFile));
        {
            CalculationResult result = iterativeSolver.next(0);
            System.out.println("input data = " + fileSystem.getFileName(inputFile));
            System.out.println(result.log);
            writeResult(inputFile, result);
        }
        while (true)
        {
            String input = System.console().readLine(
                    "write number of iterations or \'skip\':\n");
            if ("skip".equals(input))
            {
                break;
            }
            int n = parseInt(input);
            long time = System.currentTimeMillis();
            CalculationResult result = iterativeSolver.next(n);
            System.out.println("time = " + (System.currentTimeMillis() - time));
            System.out.println(result.log);
            writeResult(inputFile, result);
        }
    }

    private int parseInt(String input)
    {

        int result = 0;
        try
        {
            result = Integer.parseInt(input);
        } catch (NumberFormatException e)
        {
            return 0;
        }
        return result < 0 ? 0 : result;
    }

}
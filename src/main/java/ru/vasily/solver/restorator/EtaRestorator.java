package ru.vasily.solver.restorator;

public class EtaRestorator extends EtaOmegaRestorator
{
    public EtaRestorator(double eta)
    {
        super(eta,(3.0 - eta) / (1.0 - eta));
    }
}

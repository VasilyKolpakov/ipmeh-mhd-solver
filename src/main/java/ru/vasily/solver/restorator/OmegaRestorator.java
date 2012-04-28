package ru.vasily.solver.restorator;

public class OmegaRestorator extends EtaOmegaRestorator
{
    public OmegaRestorator(double omega)
    {
        super((3.0 - omega) / (1.0 - omega), omega);
    }
}

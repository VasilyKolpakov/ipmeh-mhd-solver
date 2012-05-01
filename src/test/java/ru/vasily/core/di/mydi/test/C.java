package ru.vasily.core.di.mydi.test;

public class C implements IC
{
    public final IB b;
    private final IA a;
//	private final IC c;

    public C(IA a, IB b)
    {
        this.a = a;
        this.b = b;
        // TODO Auto-generated constructor stub
//		this.c = c;
    }

    @Override
    public IA getA()
    {
        // TODO Auto-generated method stub
        return a;
    }
}

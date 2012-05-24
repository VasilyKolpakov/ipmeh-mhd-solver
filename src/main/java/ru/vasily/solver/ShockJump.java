package ru.vasily.solver;

public class ShockJump
{
    public final MHDValues left;
    public final MHDValues right;

    public ShockJump(MHDValues left, MHDValues right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString()
    {
        return "ShockJump{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}

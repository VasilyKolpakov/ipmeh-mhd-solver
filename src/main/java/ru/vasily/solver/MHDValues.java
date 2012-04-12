package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjects;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/7/11
 * Time: 9:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MHDValues
{
    public final double rho;
    public final double p;
    public final double u;
    public final double v;
    public final double w;
    public final double bX;
    public final double bY;
    public final double bZ;

    public MHDValues(double rho, double p, double u, double v, double w, double bX, double bY, double bZ)
    {
        this.rho = rho;
        this.p = p;
        this.u = u;
        this.v = v;
        this.w = w;
        this.bX = bX;
        this.bY = bY;
        this.bZ = bZ;
    }

    public static ValBuilder builder()
    {
        return new ValBuilder();
    }

    public static class ValBuilder
    {
        private Double rho;
        private Double p;
        private Double u;
        private Double v;
        private Double w;
        private Double bX;
        private Double bY;
        private Double bZ;

        public ValBuilder rho(double rho)
        {
            this.rho = rho;
            return this;
        }

        public ValBuilder p(double p)
        {
            this.p = p;
            return this;
        }

        public ValBuilder u(double u)
        {
            this.u = u;
            return this;
        }

        public ValBuilder v(double v)
        {
            this.v = v;
            return this;
        }

        public ValBuilder w(double w)
        {
            this.w = w;
            return this;
        }

        public ValBuilder bX(double bX)
        {
            this.bX = bX;
            return this;
        }

        public ValBuilder bY(double bY)
        {
            this.bY = bY;
            return this;
        }

        public ValBuilder bZ(double bZ)
        {
            this.bZ = bZ;
            return this;
        }

        public MHDValues build()
        {
            checkNotNullAll("one or several values is not set", rho, p, u, v, w, bX, bY, bZ);
            return new MHDValues(rho, p, u, v, w, bX, bY, bZ);
        }

        private void checkNotNullAll(String errMsg, Object... objs)
        {
            for (Object obj : objs)
            {
                checkNotNull(obj, errMsg);
            }
        }
    }

    public DataObject asDataObj()
    {
        Map<String, Double> data = ImmutableMap.<String, Double>builder()
                                               .put("rho", rho)
                                               .put("u", u)
                                               .put("v", v)
                                               .put("w", w)
                                               .put("p", p)
                                               .put("bX", bX)
                                               .put("bY", bY)
                                               .put("bZ", bZ)
                                               .build();
        return DataObjects.asDataObj(data);
    }

    public void setToArray(double[] array, double gamma)
    {
        Utils.setConservativeValues(this, array, gamma);
    }

    public static MHDValues fromDataObj(DataObject data)
    {
        return MHDValues.builder()
                        .rho(data.getDouble("rho"))
                        .p(data.getDouble("p"))
                        .u(data.getDouble("u"))
                        .v(data.getDouble("v"))
                        .w(data.getDouble("w"))
                        .bX(data.getDouble("bX"))
                        .bY(data.getDouble("bY"))
                        .bZ(data.getDouble("bZ"))
                        .build();
    }

    @Override
    public String toString()
    {
        return "MHDValues{" +
                "rho=" + rho +
                ", p=" + p +
                ", u=" + u +
                ", v=" + v +
                ", w=" + w +
                ", bX=" + bX +
                ", bY=" + bY +
                ", bZ=" + bZ +
                '}';
    }
}

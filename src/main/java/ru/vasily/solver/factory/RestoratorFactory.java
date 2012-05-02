package ru.vasily.solver.factory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.restorator.*;

import java.util.Map;

public class RestoratorFactory
{

    private static Map<String, IRestoratorFactory> factories =
            ImmutableMap.<String, IRestoratorFactory>builder()
                        .put("simple_minmod", new SimpleMinmodRestoratorFactory())
                        .put("no_op", new NoOpRestoratorFactory())
                        .put("eta_omega", new EtaOmegaRestoratorFactory())
                        .put("eta", new EtaRestoratorFactory())
                        .build();

    public ThreePointRestorator createRestorator(DataObject restoratorData)
    {
        String type = restoratorData.getString("type");
        IRestoratorFactory factory = factories.get(type);
        if (factory != null)
        {
            return factory.createRestorator(restoratorData);
        }
        else
        {
            throw new IllegalArgumentException("unsupported restorator type:" + type
                    + " only " + Joiner.on(", ").join(factories.keySet()) + " are supported ");
        }
    }

    private static class EtaRestoratorFactory implements IRestoratorFactory
    {

        @Override
        public ThreePointRestorator createRestorator(DataObject restoratorData)
        {
            double omega = restoratorData.getDouble("eta");
            return new EtaRestorator(omega);
        }
    }

    private static class EtaOmegaRestoratorFactory implements IRestoratorFactory
    {

        @Override
        public ThreePointRestorator createRestorator(DataObject restoratorData)
        {
            double eta = restoratorData.getDouble("eta");
            double omega = restoratorData.getDouble("omega");
            return new EtaOmegaRestorator(eta, omega);
        }
    }

    private static class SimpleMinmodRestoratorFactory implements IRestoratorFactory
    {

        @Override
        public ThreePointRestorator createRestorator(DataObject restoratorData)
        {
            return new MinmodRestorator();
        }
    }

    private static class NoOpRestoratorFactory implements IRestoratorFactory
    {

        @Override
        public ThreePointRestorator createRestorator(DataObject restoratorData)
        {
            return new NoOpRestorator();
        }
    }

    private interface IRestoratorFactory
    {
        ThreePointRestorator createRestorator(DataObject restoratorData);
    }
}

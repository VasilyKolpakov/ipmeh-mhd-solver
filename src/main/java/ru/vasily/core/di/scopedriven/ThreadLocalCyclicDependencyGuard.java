package ru.vasily.core.di.scopedriven;

import ru.vasily.core.di.CyclicDependencyFoundException;

import java.util.HashSet;
import java.util.Set;

public class ThreadLocalCyclicDependencyGuard
{
    private static ThreadLocal<Set<ComponentID>> componentsIdsThreadLocal = new ThreadLocal<Set<ComponentID>>()
    {
        @Override
        protected Set<ComponentID> initialValue()
        {
            return new HashSet<ComponentID>();
        }
    };

    public void assertNotTrackedAndTrackComponent(String componentKey, Class<?> clazz, ScopeDrivenDI scopeDrivenDIInstance)
    {
        Set<ComponentID> componentIDs = componentsIdsThreadLocal.get();
        ComponentID componentID = new ComponentID(componentKey, clazz, scopeDrivenDIInstance);
        if (componentIDs.contains(componentID))
        {
            throw new CyclicDependencyFoundException(String
                    .format("cyclic dependency for component having key = %s, class %s, DI instance = %s",
                            componentKey, clazz, scopeDrivenDIInstance));
        }
        componentIDs.add(componentID);
    }

    public void untrackComponent(String componentKey, Class<?> clazz, ScopeDrivenDI scopeDrivenDIInstance)
    {
        Set<ComponentID> componentIDs = componentsIdsThreadLocal.get();
        ComponentID componentID = new ComponentID(componentKey, clazz, scopeDrivenDIInstance);
        componentIDs.remove(componentID);
    }

    private static class ComponentID
    {
        private final String componentKey;
        private final Class<?> clazz;
        private final ScopeDrivenDI scopeDrivenDIInstance;

        private ComponentID(String componentKey, Class<?> clazz, ScopeDrivenDI scopeDrivenDIInstance)
        {
            this.componentKey = componentKey;
            this.clazz = clazz;
            this.scopeDrivenDIInstance = scopeDrivenDIInstance;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ComponentID that = (ComponentID) o;

            if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
            {
                return false;
            }
            if (componentKey != null ? !componentKey.equals(that.componentKey) : that.componentKey != null)
            {
                return false;
            }
            if (scopeDrivenDIInstance != scopeDrivenDIInstance)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = componentKey != null ? componentKey.hashCode() : 0;
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            result = 31 * result + (scopeDrivenDIInstance != null ? System.identityHashCode(scopeDrivenDIInstance) : 0);
            return result;
        }
    }

}

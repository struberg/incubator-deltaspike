/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.core.util;

import javax.enterprise.inject.spi.Extension;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Support for Containers with 'hierarchic BeanManagers'
 */
public final class ParentExtensionStorage
{

    private static Set<ExtensionStorageInfo> extensionStorage = new HashSet<ExtensionStorageInfo>();

    private ParentExtensionStorage()
    {
        // utility class ct
    }

    /**
     * Add info about an Extension to our storage
     * This method is usually called during boostrap via {@code &#064;Observes BeforeBeanDiscovery}.
     */
    public static synchronized void addExtension(Extension extension)
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        extensionStorage.add(new ExtensionStorageInfo(classLoader, extension));
    }

    /**
     * Remove the given Extension and all it's 'children' from our storage.
     * This method is usually called during shutdown via {@code &#064;Observes BeforeShutdown}.
     */
    public static synchronized void removeExtension(Extension extension)
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        Iterator<ExtensionStorageInfo> extIt = extensionStorage.iterator();
        while (extIt.hasNext())
        {
            ExtensionStorageInfo extensionInfo = extIt.next();
            ClassLoader extCl = extensionInfo.getClassLoader();
            if (extCl == null || // weak reference case
                extension.getClass().equals(extensionInfo.getExtension().getClass()))
            {
                do
                {
                    if (extCl == null || extCl.equals(classLoader))
                    {
                        extIt.remove();
                        continue;
                    }
                    extCl = extCl.getParent();
                } while (extCl != null);
            }
        }
    }

    /**
     * @return the Extension from the same type but registered in a hierarchic 'parent' BeanManager
     */
    public static synchronized <T extends Extension> T getParentExtension(Extension extension)
    {
        ClassLoader parentClassLoader = ClassUtils.getClassLoader(null).getParent();

        Iterator<ExtensionStorageInfo> extIt = extensionStorage.iterator();
        while (extIt.hasNext())
        {
            ExtensionStorageInfo extensionInfo = extIt.next();
            if (extensionInfo.getClassLoader() != null && // weak reference case
                extension.getClass().equals(extensionInfo.getExtension().getClass()) &&
                extensionInfo.getClassLoader().equals(parentClassLoader))
            {
                return (T) extensionInfo.getExtension();
            }
        }
        return null;
    }


    /**
     * Information about an Extension instance and in which classloader it got used
     */
    private static class ExtensionStorageInfo
    {
        // we use WeakReferences to allow perfect unloading of any webapp ClassLoader
        private final WeakReference<ClassLoader> classLoader;
        private final WeakReference<Extension> extension;

        public ExtensionStorageInfo(ClassLoader classLoader, Extension extension)
        {
            this.classLoader = new WeakReference<ClassLoader>(classLoader);
            this.extension = new WeakReference<Extension>(extension);
        }

        ClassLoader getClassLoader()
        {
            return classLoader.get();
        }

        Extension getExtension()
        {
            return extension.get();
        }
    }
}

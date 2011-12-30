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
package org.apache.deltaspike.example.beanmanagement;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.example.CdiContainer;
import org.apache.deltaspike.example.echo.EchoService;

import java.util.List;
import java.util.logging.Logger;

/**
 * Example which illustrates the usage of {@inheritDoc BeanProvider}
 */
public class SimpleBeanLookupExample
{
    private static final Logger LOG = Logger.getLogger(SimpleBeanLookupExample.class.getName());

    /**
     * Entry point
     * @param args currently not used
     */
    public static void main(String[] args)
    {
        CdiContainer.start();

        List<EchoService> echoServiceList = BeanProvider.getContextualReferences(EchoService.class, false, true);

        for(EchoService echoService : echoServiceList)
        {
            LOG.info(echoService.echo("Hello CDI beans!"));
        }

        LOG.info("---");

        echoServiceList = BeanProvider.getContextualReferences(EchoService.class, false, false);

        for(EchoService echoService : echoServiceList)
        {
            LOG.info(echoService.echo("Hello non dependent CDI scoped beans!"));
        }

        CdiContainer.stop();
    }
}

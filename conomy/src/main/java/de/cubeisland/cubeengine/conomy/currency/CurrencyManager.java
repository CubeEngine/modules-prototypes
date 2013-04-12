/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.conomy.currency;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.config.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CurrencyManager
{
    private THashMap<String, Currency> currencies = new THashMap<String, Currency>();
    private Conomy module;
    private ConomyConfiguration config;
    private Currency mainCurrency;

    public CurrencyManager(Conomy module, ConomyConfiguration config)
    {
        this.module = module;
        this.config = config;
    }

    public void load()
    {
        for (Map.Entry<String, CurrencyConfiguration> entry : config.currencies.entrySet())
        {
            this.currencies.put(entry.getKey().toLowerCase(), new Currency(this, entry.getKey(), entry.getValue()));
        }
        this.mainCurrency = this.currencies.get(config.currencies.keySet().iterator().next().toLowerCase());

        if (this.config.relations != null)
        {
            for (Entry<String, Map<String, Double>> entry : this.config.relations.entrySet())
            {
                Currency currency = this.getCurrencyByName(entry.getKey().toLowerCase());
                if (currency == null)
                {
                    module.getLog().warning("Unknown currency in relations! " + entry.getKey());
                }
                for (Entry<String, Double> relation : entry.getValue().entrySet())
                {
                    Currency relatedCurrency = this.getCurrencyByName(relation.getKey().toLowerCase());
                    if (relatedCurrency == null)
                    {
                        module.getLog().warning("Unknown currency in relations! " + relation.getKey());
                        continue;
                    }
                    currency.addConversionRate(relatedCurrency, relation.getValue());
                    relatedCurrency.addConversionRate(currency, 1 / relation.getValue());
                }
            }
        }
    }

    public Collection<Currency> getAllCurrencies()
    {
        return this.currencies.values();
    }

    public Currency getMainCurrency()
    {
        return mainCurrency;
    }

    public Currency getCurrencyByName(String currencyName)
    {
        if (currencyName == null)
            return null;
        return this.currencies.get(currencyName.toLowerCase());
    }

    public Currency matchCurrency(String amountString)
    {
        return this.matchCurrency(amountString, true).iterator().next();
    }

    public Collection<Currency> matchCurrency(String amountString, boolean returnDefaultIfNotFound)
    {
        List<Currency> found = new ArrayList<Currency>();
        for (Currency currency : this.currencies.values()) // match currencyNames & long names
        {
            if (amountString.toLowerCase().contains(currency.getName().toLowerCase()))
            {
                found.add(currency);
                continue;
            }
            else
            {
                for (String sub : currency.getAllNames().keySet())
                {
                    if (amountString.toLowerCase().contains(sub))
                    {
                        found.add(currency);
                        continue;
                    }
                }
            }
        }
        if (found.isEmpty()) // match symbols
        {
            for (Currency currency : this.currencies.values())
            {
                for (String subSymbol : currency.getAllSymbols().keySet())
                {
                    if (amountString.toLowerCase().contains(subSymbol))
                    {
                        found.add(currency);
                        continue;
                    }
                }
            }
        }
        if (found.isEmpty())
        {
            found.add(this.mainCurrency);
        }
        return found;
    }
}
/*
 * Copyright 2017 ObjectBox Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.objectbox.relation;

import org.junit.Test;

import java.util.List;

import io.objectbox.query.QueryConsumer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelationEagerTest extends AbstractRelationTest {

    @Test
    public void testEagerToMany() {
        Customer customer = putCustomer();
        putOrder(customer, "Bananas");
        putOrder(customer, "Oranges");

        Customer customer2 = putCustomer();
        putOrder(customer2, "Apples");

        // full list
        List<Customer> customers = customerBox.query().eager(Customer_.orders).build().find();
        assertEquals(2, customers.size());
        assertTrue(((ToMany) customers.get(0).getOrders()).isResolved());
        assertTrue(((ToMany) customers.get(1).getOrders()).isResolved());

        // full list paginated
        customers = customerBox.query().eager(Customer_.orders).build().find(0, 10);
        assertEquals(2, customers.size());
        assertTrue(((ToMany) customers.get(0).getOrders()).isResolved());
        assertTrue(((ToMany) customers.get(1).getOrders()).isResolved());

        // list with eager limit
        customers = customerBox.query().eager(1, Customer_.orders).build().find();
        assertEquals(2, customers.size());
        assertTrue(((ToMany) customers.get(0).getOrders()).isResolved());
        assertFalse(((ToMany) customers.get(1).getOrders()).isResolved());

        // forEach
        final int count[] = {0};
        customerBox.query().eager(1, Customer_.orders).build().forEach(new QueryConsumer<Customer>() {
            @Override
            public void accept(Customer data) {
                assertEquals(count[0] == 0, ((ToMany) data.getOrders()).isResolved());
                count[0]++;
            }
        });
        assertEquals(2, count[0]);

        // first
        customer = customerBox.query().eager(Customer_.orders).build().findFirst();
        assertTrue(((ToMany) customer.getOrders()).isResolved());

        // unique
        customerBox.remove(customer);
        customer = customerBox.query().eager(Customer_.orders).build().findUnique();
        assertTrue(((ToMany) customer.getOrders()).isResolved());
    }

    @Test
    public void testEagerToSingle() {
        Customer customer = putCustomer();
        putOrder(customer, "Bananas");
        putOrder(customer, "Oranges");

        // full list
        List<Order> orders = orderBox.query().eager(Order_.customer).build().find();
        assertEquals(2, orders.size());
        assertTrue(orders.get(0).customer__toOne.isResolved());
        assertTrue(orders.get(1).customer__toOne.isResolved());

        // full list paginated
        orders = orderBox.query().eager(Order_.customer).build().find(0, 10);
        assertEquals(2, orders.size());
        assertTrue(orders.get(0).customer__toOne.isResolved());
        assertTrue(orders.get(1).customer__toOne.isResolved());

        // list with eager limit
        orders = orderBox.query().eager(1, Order_.customer).build().find();
        assertEquals(2, orders.size());
        assertTrue(orders.get(0).customer__toOne.isResolved());
        assertFalse(orders.get(1).customer__toOne.isResolved());

        // forEach
        final int count[] = {0};
        customerBox.query().eager(1, Customer_.orders).build().forEach(new QueryConsumer<Customer>() {
            @Override
            public void accept(Customer data) {
                assertEquals(count[0] == 0, ((ToMany) data.getOrders()).isResolved());
                count[0]++;
            }
        });
        assertEquals(1, count[0]);

        // first
        Order order = orderBox.query().eager(Order_.customer).build().findFirst();
        assertTrue(order.customer__toOne.isResolved());

        // unique
        orderBox.remove(order);
        order = orderBox.query().eager(Order_.customer).build().findUnique();
        assertTrue(order.customer__toOne.isResolved());
    }

}

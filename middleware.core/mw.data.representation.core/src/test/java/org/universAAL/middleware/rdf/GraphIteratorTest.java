package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;
import junit.framework.TestCase;

public class GraphIteratorTest extends TestCase {

    class Triple {
	Object[] o = new Object[3];

	Triple(Object o0, Object o1, Object o2) {
	    o[0] = o0;
	    o[1] = o1;
	    o[2] = o2;
	}

	public String toString() {
	    return o[0].toString() + "\t" + o[1].toString() + "\t"
		    + o[2].toString();
	}
    }

    int num = 20;
    Resource r[] = new Resource[num];
    String p[] = new String[num];
    Set tripleSet;
    Set resources;

    public void test1() {
	Resource r = new Resource("root_resource");
	r.setProperty("prop1", new Resource("child1"));

	Iterator it = GraphIterator.getIterator(r);
	while (it.hasNext()) {
	    GraphIteratorElement el = (GraphIteratorElement) it.next();
	    assertTrue(el != null);
	    assertTrue("root_resource".equals(((Resource) el.getSubject())
		    .getURI()));
	    assertTrue("prop1".equals(el.getPredicate()));
	    assertTrue(el.getObject() instanceof Resource);
	    assertTrue("child1".equals(((Resource) el.getObject()).getURI()));
	}
    }

    private void init() {
	tripleSet = new HashSet();
	resources = new HashSet();
	for (int i = 0; i < num; i++)
	    r[i] = new Resource("resource_" + i);
	for (int i = 0; i < num; i++)
	    p[i] = "prop_" + i;
    }

    private void add(Resource o0, String o1, Object o2) {
	o0.setProperty(o1, o2);
	if (o2 instanceof List) {
	    for (Iterator it = ((List) o2).iterator(); it.hasNext();) {
		Object o = it.next();
		Triple t = new Triple(o0, o1, o);
		tripleSet.add(t.toString());
		if (o instanceof Resource)
		    resources.add(o);
	    }
	} else {
	    Triple t = new Triple(o0, o1, o2);
	    tripleSet.add(t.toString());
	    if (o2 instanceof Resource)
		resources.add(o2);
	}
	resources.add(o0);
    }

    private void add(int o0, int o1, int o2) {
	add(r[o0], p[o1], r[o2]);
    }

    private void verify() {
	Iterator it = GraphIterator.getIterator(r[0]);
	int prevDepth = 0;
	// System.out.println("iterating over a graph with " + tripleSet.size()
	// + " triples.");
	while (it.hasNext()) {
	    GraphIteratorElement el = (GraphIteratorElement) it.next();
	    assertTrue(el != null);
	    Triple t = new Triple(el.getSubject(), el.getPredicate(),
		    el.getObject());
	    assertTrue(tripleSet.contains(t.toString()));
	    tripleSet.remove(t.toString());
	    assertTrue(prevDepth + 2 > el.getDepth());
	    prevDepth = el.getDepth();
	    // System.out.println(el.getDepth() + "  " + t.toString());
	}
	assertTrue(tripleSet.size() == 0);
    }

    public void test2() {
	init();

	add(0, 0, 1);
	add(0, 1, 1);
	add(0, 2, 2);
	add(1, 2, 2);
	add(2, 3, 0);
	add(r[1], p[4], Integer.valueOf(42));
	ArrayList arr = new ArrayList();
	arr.add(r[0]);
	arr.add(r[2]);
	arr.add(r[3]);
	add(r[1], p[5], arr);

	verify();
    }

    public void test3() {
	init();

	add(0, 0, 1);
	add(0, 1, 1);
	add(0, 2, 2);
	add(1, 2, 2);

	// test without cycles
	Set resources2 = new HashSet(resources);
	Iterator it = GraphIterator.getResourceIterator(r[0]);
	while (it.hasNext()) {
	    Resource r = (Resource) it.next();
	    // System.out.println(r.getURI());
	    assertTrue(resources2.contains(r));
	    resources2.remove(r);
	}
	assertTrue(resources2.isEmpty());

	add(2, 3, 0);
	add(r[1], p[4], Integer.valueOf(42));
	ArrayList arr = new ArrayList();
	arr.add(r[0]);
	arr.add(r[2]);
	arr.add(r[3]);
	add(r[1], p[5], arr);

	// test without cycles (and list and literal)
	resources2 = new HashSet(resources);
	it = GraphIterator.getResourceIterator(r[0]);
	while (it.hasNext()) {
	    Resource r = (Resource) it.next();
	    // System.out.println(r.getURI());
	    assertTrue(resources2.contains(r));
	    resources2.remove(r);
	}
	assertTrue(resources2.isEmpty());
    }

    /*
     * public void test4() { for (int loops = 0; loops < 500; loops++) { init();
     * 
     * resources.add(r[0]); for (int iRes = 0; iRes < 50; iRes++) { int numProps
     * = (int) (Math.random() * 6); Resource res = (Resource)
     * resources.toArray()[(int) (Math .random() * (resources.size() - 1))]; for
     * (int iProp = 0; iProp < numProps; iProp++) { String propURI = p[(int)
     * (Math.random() * (num - 1))]; if (res.getProperty(propURI) != null)
     * continue; if (Math.random() > 0.8) { // make a list ArrayList arr = new
     * ArrayList(); int numListEl = (int) (Math.random() * (num - 1)); for (int
     * iListEl = 0; iListEl < numListEl; iListEl++) { Resource obj = (Resource)
     * r[(int) (Math.random() * (num - 1))]; if (!arr.contains(obj))
     * arr.add(obj); } add(res, propURI, arr); } else { Resource obj =
     * (Resource) r[(int) (Math.random() * (num - 1))]; add(res, propURI, obj);
     * } } }
     * 
     * System.out.println(r[0].toStringRecursive());
     * 
     * java.util.Comparator c = new java.util.Comparator() { public int
     * compare(Object arg0, Object arg1) { return ((Resource)
     * arg0).getURI().compareTo( ((Resource) arg1).getURI()); } }; List sorted =
     * new ArrayList(resources); java.util.Collections.sort(sorted, c); for (int
     * i = 0; i < sorted.size(); i++) System.out.println("Resource: " +
     * sorted.get(i));
     * 
     * sorted = new ArrayList(tripleSet); java.util.Collections.sort(sorted);
     * for (int i = 0; i < sorted.size(); i++) System.out.println("tripleSet: "
     * + sorted.get(i));
     * 
     * System.out.println("Loop: " + loops); verify(); } }
     */
}

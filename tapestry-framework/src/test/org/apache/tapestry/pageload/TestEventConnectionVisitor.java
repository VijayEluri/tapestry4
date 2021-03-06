package org.apache.tapestry.pageload;

import org.apache.tapestry.BaseComponentTestCase;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IForm;
import org.apache.tapestry.IPage;
import org.apache.tapestry.internal.event.ComponentEventProperty;
import org.apache.tapestry.internal.event.IComponentEventInvoker;
import org.apache.tapestry.spec.ComponentSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.expect;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests functionality of {@link EventConnectionVisitor}.
 */
@Test
public class TestEventConnectionVisitor extends BaseComponentTestCase {

    public void test_Wire_Component_Event()
    {
        IComponentSpecification spec = new ComponentSpecification();
        spec.addEventListener("comp1", new String[] {"onClick"}, "testFoo", null, false, false, false, false);

        IComponent comp = newComponent(spec, "comp1", "path/", "Home/");
        IComponentEventInvoker invoker = newMock(IComponentEventInvoker.class);

        invoker.addEventListener("path/comp1", spec);

        replay();

        EventConnectionVisitor v = new EventConnectionVisitor();
        v.setEventInvoker(invoker);

        v.visitComponent(comp);

        verify();
    }

    public void test_Wire_Element_Form_Events()
    {
        IComponentSpecification spec = new ComponentSpecification();
        spec.addElementEventListener("elem1", new String[] {"onClick"}, "testFoo", "form", false, false, false);

        IComponent comp = newComponent(spec);
        IPage page = newMock(IPage.class);
        IForm form = newMock(IForm.class);
        IComponentEventInvoker invoker = newMock(IComponentEventInvoker.class);

        Map comps = new HashMap();
        comps.put("form", form);

        expect(comp.getPage()).andReturn(page).anyTimes();
        expect(page.getComponents()).andReturn(comps).anyTimes();
        expect(comp.getComponents()).andReturn(Collections.EMPTY_MAP).anyTimes();
        expect(form.getComponents()).andReturn(Collections.EMPTY_MAP).anyTimes();

        expect(form.getId()).andReturn("form").anyTimes();
        expect(form.getExtendedId()).andReturn("path/form").anyTimes();

        invoker.addFormEventListener("path/form", spec);
        invoker.addFormEventListener("path/form", spec);

        replay();

        EventConnectionVisitor v = new EventConnectionVisitor();
        v.setEventInvoker(invoker);

        v.visitComponent(comp);
        v.visitComponent(comp);

        verify();
    }

    public void test_Spec_Rewire_Id()
    {
        IComponentSpecification spec = newMock(IComponentSpecification.class);
        IComponentEventInvoker invoker = newMock(IComponentEventInvoker.class);
        IComponent comp = newComponent(spec, "comp1", "path/", "Home/");

        ComponentEventProperty p = new ComponentEventProperty("comp1");
        p.addListener(new String[] {"onClick"}, "testFoo", null, false, false, false, false);

        Map compEvents = new HashMap();
        compEvents.put("comp1", p);
        
        expect(spec.getComponentEvents()).andReturn(compEvents);
        expect(spec.getElementEvents()).andReturn(Collections.EMPTY_MAP);

        invoker.addEventListener("path/comp1", spec);
        spec.rewireComponentId("comp1", "path/comp1", "Home/path/comp1");

        replay();

        EventConnectionVisitor v = new EventConnectionVisitor();
        v.setEventInvoker(invoker);

        v.visitComponent(comp);

        verify();
    }

    IComponent newComponent(IComponentSpecification spec, String findCompId, Object... args)
    {
        IComponent comp = newComponent(spec);
        IPage page = newMock(IPage.class);

        expect(comp.getPage()).andReturn(page).anyTimes();
        
        Map comps = new HashMap();
        comps.put(findCompId, comp);

        expect(page.getComponents()).andReturn(comps).anyTimes();
        expect(comp.getComponents()).andReturn(null).anyTimes();

        if (args.length > 0)
        {    
            expect(comp.getExtendedId()).andReturn(args[0] + findCompId).anyTimes();
        }

        if (args.length > 1)
        {
            expect(comp.getIdPath()).andReturn((String)args[1] + (String)args[0] + findCompId).anyTimes();
        }

        return comp;
    }

    IComponent newComponent(IComponentSpecification spec)
    {
        IComponent comp = newMock(IComponent.class);

        checkOrder(comp, false);
        expect(comp.getSpecification()).andReturn(spec).anyTimes();

        return comp;
    }
}

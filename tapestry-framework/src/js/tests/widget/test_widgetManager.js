dojo.registerModulePath("tapestry", "../tapestry-4.1.6");

dojo.require("dojo.ns");
dojo.require("dojo.widget.*");
dojo.require("tapestry.core");
dojo.require("tapestry.test");
dojo.require("tapestry.widget.Widget");

function test_syncfailure_widget(){
	try {
		tapestry.widget.synchronizeWidgetState("bs", "NonExistant", {});
	} catch (e) { jum.assertTrue("test2", e instanceof Error); return; }
	throw new JUMAssertFailure("Previous test should have failed.");
}

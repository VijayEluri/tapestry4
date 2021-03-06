dojo.require("dojo.logging.Logger");
dojo.require("dojo.io.RhinoIO");

function test_debug_log(){
	dojo.log.debug("Debug message.");
	dojo.log.info("Info message");
	dojo.log.err("Error message.");
	dojo.log.warn("Warning message");
	dojo.log.crit("Critical message");
}

function test_debug_logLevel(){
	dojo.log.info("info msg");

	dojo.log.setLevel(dojo.log.getLevel("WARNING"));
	dojo.log.info("SHOULD NOT SEE THIS");
	var last = dojo.logging.logQueueHandler.data.pop();
	jum.assertEquals("info msg", last.message);

	var currLength = dojo.logging.logQueueHandler.data.length;
	dojo.log.debug("DEFINITELY SHOULDNT see this");
	jum.assertEquals("logmsglength", currLength, dojo.logging.logQueueHandler.data.length);

	dojo.log.setLevel(dojo.log.getLevel("DEBUG"));
}

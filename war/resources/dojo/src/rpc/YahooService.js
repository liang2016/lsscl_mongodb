/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/



dojo.provide("dojo.rpc.YahooService");
dojo.require("dojo.rpc.RpcService");
dojo.require("dojo.rpc.JsonService");
dojo.require("dojo.json");
dojo.require("dojo.uri.*");
dojo.require("dojo.io.ScriptSrcIO");
dojo.rpc.YahooService = function (appId) {
	this.appId = appId;
	if (!appId) {
		this.appId = "dojotoolkit";
		dojo.debug("please initialize the YahooService class with your own", "application ID. Using the default may cause problems during", "deployment of your application");
	}
	if (djConfig["useXDomain"] && !djConfig["yahooServiceSmdUrl"]) {
		dojo.debug("dojo.rpc.YahooService: When using cross-domain Dojo builds," + " please save yahoo.smd to your domain and set djConfig.yahooServiceSmdUrl" + " to the path on your domain to yahoo.smd");
	}
	this.connect(djConfig["yahooServiceSmdUrl"] || dojo.uri.moduleUri("dojo.rpc", "yahoo.smd"));
	this.strictArgChecks = false;
};
dojo.inherits(dojo.rpc.YahooService, dojo.rpc.JsonService);
dojo.lang.extend(dojo.rpc.YahooService, {strictArgChecks:false, bind:function (method, parameters, deferredRequestHandler, url) {
	var params = parameters;
	if ((dojo.lang.isArrayLike(parameters)) && (parameters.length == 1)) {
		params = parameters[0];
	}
	params.output = "json";
	params.appid = this.appId;
	dojo.io.bind({url:url || this.serviceUrl, transport:"ScriptSrcTransport", content:params, jsonParamName:"callback", mimetype:"text/json", load:this.resultCallback(deferredRequestHandler), error:this.errorCallback(deferredRequestHandler), preventCache:true});
}});


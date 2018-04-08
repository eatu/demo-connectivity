sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/ui/model/odata/v2/ODataModel"
], function(Controller, JSONModel, ODataModel) {
	"use strict";

	return Controller.extend("com.sap.core.connectivity.controller.SCCProductDemo", {

		onInit: function() {
			var oView = this.getView();
			var oModel = new ODataModel({serviceUrl: "./data-eu/", useBatch: false});
			oView.setModel(oModel);
		}

	});

});
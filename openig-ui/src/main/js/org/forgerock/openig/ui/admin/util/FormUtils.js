/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

define([
    "jquery",
    "lodash",
    "i18next",
    "org/forgerock/commons/ui/common/main/ValidatorsManager"
], (
    $,
    _,
    i18n,
    validatorsManager
) => ({
    extendControlsSettings (controls, options) {
        if (!options.defaultValidatorEvent) {
            options.defaultValidatorEvent = "keyup blur";
        }
        _.forEach(controls, (c) => {
            if (options.autoTitle && options.autoTitle === true && c.title === undefined) {
                c.title = `${options.translatePath}.${c.name}`;
            }
            if (options.autoHint && options.autoHint === true && c.hint === undefined) {
                c.hint = `${options.translatePath}.${c.name}Hint`;
            }
            if (options.autoPlaceHolder && options.autoPlaceHolder === true && c.placeholder === undefined) {
                c.placeholder = `${options.translatePath}.${c.name}PlaceHolder`;
            }
            if (options.defaultControlType && c.controlType === undefined) {
                c.controlType = options.defaultControlType;
            }
            if (c.validatorEvent === undefined) {
                c.validatorEvent = options.defaultValidatorEvent;
            }

            if (c.controlType) {
                this.extendControlsSettings(c.controls, options);
            }
        });
    },

    fillPartialsByControlType (controls) {
        const self = this;
        _.forEach(controls, (c) => {
            // 'control()' function is used for dynamically select the partial to be executed within handelbars template
            c.control = function () {
                switch (this.controlType) {
                    case "slider": return "templates/openig/admin/common/form/SliderControl";
                    case "edit": return "templates/openig/admin/common/form/EditControl";
                    case "multiselect": return "templates/openig/admin/common/form/MultiSelectControl";
                    case "dropdownedit": return "templates/openig/admin/common/form/DropdownEditControl";
                    case "group":
                        self.fillPartialsByControlType(c.controls);
                        return "templates/openig/admin/common/form/GroupControl";
                    default: return this.template;
                }
            };
        });
    },

    initializeMultiSelect (control) {
        const input = $(control);
        const delimiter = input.data("delimiter");
        const tags = input.val().split(delimiter);
        const mandatory = input.data("mandatory");
        const predefinedOptions = _.map(input.data("options").split(delimiter), (option) => (
            {
                value: option,
                text: option
            }
        ));

        return input.selectize({
            delimiter,
            persist: true,
            sortField: "text",
            options: predefinedOptions,
            create (input) {
                return {
                    value: input,
                    text: input
                };
            },
            onInitialize () {
                if (tags) {
                    _.forEach(tags, (tag) => {
                        this.addOption({ value: tag, text: tag });
                    });
                    const defaultValues = tags;
                    defaultValues.push(this.getValue().split(/\s* \s*/));
                    defaultValues.push(mandatory);
                    this.setValue(
                        _(defaultValues)
                            .flattenDeep()
                            .uniq()
                            .value()
                    );
                }
            },
            onItemRemove (value) {
                if (mandatory === value) {
                    this.addItem(value);
                }
            }
        });
    },

    isFormValid (form) {
        const deferred = $.Deferred();
        const validatorResults = [];
        _.forEach($(form).find("input"), (control) => {
            const input = $(control);
            const valRes = this.evaluateAllValidatorsForField(input, form);
            validatorResults.push(valRes);
        });
        $.when.apply($, validatorResults).then((...args) => {
            if ($.inArray(false, args) === -1) {
                deferred.resolve();
            } else {
                deferred.reject();
            }
        }
        );
        return deferred;
    },

    evaluateAllValidatorsForField (element, container) {
        const validatorsRegistered = element.attr("data-validator");
        const deferred = $.Deferred();
        if (validatorsRegistered) {
            // wait for all promises to be resolved from the various valiators named on the element
            return $.when.apply($, _.map(validatorsRegistered.split(" "), (validatorName) => (
                validatorsManager.evaluateValidator(validatorName, element, container)
            ))).then((...args) => {
                const allFailures = _(args)
                    .toArray()
                    .flatten()
                    .filter((value) => (
                        value !== undefined
                    ))
                    .uniq()
                    .value();

                if (allFailures.length) {
                    // Failed
                    return $.Deferred().resolve(false);
                } else {
                    //Succeeded
                    return $.Deferred().resolve(true);
                }
            });
        } else {
            // Succeded
            return deferred.resolve(true);
        }
    },

    // Callback method for form2js which convert string values into right types
    convertToJSTypes (element) {
        if (!element.tagName || element.tagName.toLowerCase() !== "input" || !element.type) {
            return;
        }
        switch (element.type.toLowerCase()) {
            case "number": return { name: element.name, value: element.valueAsNumber };
            case "checkbox": return { name: element.name, value: element.checked };
        }
    }

})
);

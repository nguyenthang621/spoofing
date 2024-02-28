First, we set the data url which is `apiEndpoint` to know where the data came from. For example:

```yaml
apiEndpoint: /services/iax-mag/operator-codes
```

Next, is the `fields` tag, inside this `fields` elements, we create all the elements of the form. For example:

```yaml
fields:
  - key: name
    type: input
    templateOptions:
      label: Operator
```

To create the element inside the form, we use the `key` element to define the variable name of the element. For example:

```yaml
- key: name
```

Next, is the type of the element inside the form, we use the `type` tag. For example:

```yaml
- key: name
  type: input
```

Finally, we use the `templateOptions` tag to define all the attributes of the element. For example, we create the label `Operator` for the element using the `label` tag inside the `templateOptions` tag.

```yaml
- key: name
  type: input
  templateOptions:
    label: Operator
```

## Available Field Types

### Basic Field Types

```yaml
# Check Box
- key: checkbox
  type: checkbox
  templateOptions:
    label: Checkbox
# WYSIWYG
- key: wysiwyg
  type: quill
  templateOptions:
    label: What you see is what you get
# Text Input
- key: input
  type: input
  templateOptions:
    label: Input
# Radio
- key: radio
  type: radio
  templateOptions:
    options:
      - value: A
        label: Option A
      - value: B
        label: Option B
      - value: C
        label: Option C
    label: Radio
# Single Select
- key: select
  type: select
  templateOptions:
    options:
      - value: PlanA
        label: Plan A
      - value: planB
        label: Plan B
      - value: planC
        label: Plan C
    label: Select
# Multiple Select
- key: selectMulti
  type: select
  templateOptions:
    options:
      - value: '1'
        label: Option 1
      - value: '2'
        label: Option 2
      - value: '3'
        label: Option 3
    multiple: true
    label: Multiple Select
# Checkbox group
- key: checkGroup
  type: multicheckbox
  defaultValue:
    A: true
    B: false
  templateOptions:
    label: Checkbox Group
    options:
      - key: A
        value: Label for checkbox A
      - key: B
        value: Label for checkbox B
      - key: C
        value: Label for checkbox C
# Hidden field
- key: doNotShow
  defaultValue: 'hiddenValue'
  hideExpression: true
```

### Advance Field Types

Form Control:

- [Remote Form](fields/remote-form)
- [File Upload using `file-gridfs` and `file-upload`](fields/file-gridfs)
- [ng-select and tags: Select remote data and populate into a multiple select box](fields/ng-select)
- [Repeat Section: Embed multiple documents in one](fields/repeat)
- [CRUD Table: Manage 1-N relationship using separate endpoint](fields/crud-table)
- [Button: retrieve remote value](fields/button)

Wrappers:

- [Tab and wizards](fields/tabset)

## Link Fields Value together

- Predefined value

```yaml
- key: preset
  type: input
  hideExpression: true
  defaultValue: PRESET_VALUE
```

- Populate value from another field:

```yaml
- key: startDate
  type: date
- key: endDate
  type: date
  expressionProperties:
    model.endDate: 'formState.moment(model.startDate).add(3, "day").format("YYYY-MM-DD")'
  templateOptions:
    description: This field will be 3 days behind startDate
```

- Use `input type=hidden`

```yaml
- key: preset
  type: input
  hideExpression: true
  expressionProperties:
    model.preset: formState.mainModel.referenceField
  templateOptions:
    type: hidden
```

##### Panel

- Inside the `fieldGroup`, we can use the `wrappers` tag to set the component show up in a panel form. For example:

```yaml
- type: tabset
  fieldGroup:
    - key: auto-reply
      templateOptions:
        label: SMS Auto Reply
      fieldGroup:
        - key: response-messages
          wrappers:
            - panel
          templateOptions:
            label: Response Messages
```

#### Select

- We use the `type` tag as `select` to create the select box in the form. For example:

```yaml
- key: tag
  type: select
  templateOptions:
    label: Tag
    required: true
    options:
      - label: 'TAG_SOURCE_TELEMATICS_ID'
        value: '0010'
```

- In the define above, in the `templateOptions` tag, we can use `required` tag with `true` or `false` to define if the select box is required in the form or not.
- Also, we use the `options` tag to define all the elements of the select box with `label` tag as the label of the element and `value` tag as the value of the element

#### Quill

- We define the value of the `type` tag as `quill` when we want to create a text editor elements in the form. For example:

```yaml
- key: description
  type: quill
  templateOptions:
    label: Description
```

#### Fieldset

- We could use `wrappers` tag as `['fieldset']` to implement all the element inside the key in one field set. For example, we want to create a field set with the `Last Name` and `First Name` inside, and call it `Full Name`:

```yaml
- key: name
  wrappers: ['fieldset']
  templateOptions:
    label: Full Name
  fieldGroup:
    - key: firstname
      type: input
      templateOptions:
        label: 'First Name'
    - key: lastname
      type: input
      templateOptions:
        label: 'Last Name'
```

#### Select Table

Let user select multiple values from reference entities

```yaml
- key: users
  type: select-table
  templateOptions:
    label: Select Users
    # Where to retrieve the user list
    apiEndpoint: api/users
    # Additional parameters
    params:
      activated: true
    # Columns to display in table
    columns:
      - login
      - firstName
      - lastName
    # Allow select one user many times or not
    hideSelected: false
    # How to sort and paginate the result
    itemsPerPage: 5
    reverse: true
    predicate: updatedAt
  fieldArray:
    fieldGroup:
```

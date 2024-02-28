import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FormBuilderService {
  // + Field Types configuration - key = input type, value = formly configuration
  public fieldTypes = {
    input: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    textarea: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    checkbox: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    radio: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } },
      {
        key: 'options',
        type: 'repeat',
        templateOptions: { addText: 'Add an Options' },
        fieldArray: {
          fieldGroupClassName: 'row',
          fieldGroup: [
            { key: 'value', type: 'input', className: 'col-6', templateOptions: { label: 'Value', required: true } },
            { key: 'label', type: 'input', className: 'col-6', templateOptions: { label: 'Label', required: true } }
          ]
        }
      }
    ],
    select: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } },
      {
        key: 'multiple',
        type: 'checkbox',
        templateOptions: { label: 'Multiple', description: 'Is this field allow multiple values at same time?' }
      },
      {
        key: 'options',
        type: 'repeat',
        templateOptions: { addText: 'Add an Options' },
        fieldArray: {
          fieldGroupClassName: 'row',
          fieldGroup: [
            { key: 'value', type: 'input', className: 'col-6', templateOptions: { label: 'Value', required: true } },
            { key: 'label', type: 'input', className: 'col-6', templateOptions: { label: 'Label', required: true } }
          ]
        }
      }
    ],
    // Other extra types
    // + date-picker
    date: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // - datetimepicker
    datetime: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // + time-picker
    time: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // + quill-editor
    quill: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'placeholder', type: 'input', templateOptions: { label: 'Placeholder', placeholder: 'Field placeholder' } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // + file-gridfs
    'file-gridfs': [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } },
      { key: 'isImage', type: 'checkbox', templateOptions: { label: 'Is Image', description: 'Is this file support image only' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // + ng-selext
    'ng-select': [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      {
        key: 'apiEndpoint',
        type: 'input',
        templateOptions: { label: 'API Endpoint', placeholder: 'Remote API Endpoint to retrieve data' }
      },
      { key: 'description', type: 'textarea', templateOptions: { label: 'Description', description: 'Field description' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } },
      { key: 'hideSelected', type: 'checkbox', templateOptions: { label: 'Hide Selected', description: 'Hide Selected' } },
      { key: 'val', type: 'input', templateOptions: { placeholder: 'Field Value', required: true } },
      { key: 'key', type: 'input', templateOptions: { placeholder: 'Field Key', required: true } }
    ],
    'select-table': [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      {
        key: 'apiEndpoint',
        type: 'input',
        templateOptions: { label: 'API Endpoint', placeholder: 'Remote API Endpoint to retrieve data' }
      },
      { key: 'columns', type: 'input', templateOptions: { label: 'Columns', placeholder: 'Columns to show up in table' } },
      { key: 'required', type: 'checkbox', templateOptions: { label: 'Required', description: 'Is this field required?' } }
    ],
    // + quill-editor
    button: [
      { key: 'label', type: 'input', templateOptions: { label: 'Label', placeholder: 'Field label', required: true } },
      { key: 'apiEndpoint', type: 'input', templateOptions: { label: 'API Endpoint', placeholder: 'Api Endpoint' } },
      {
        key: 'btnType',
        type: 'select',
        templateOptions: {
          label: 'Button Type',
          options: [
            { value: 'primary', label: 'Primary' },
            { value: 'secondary', label: 'Secondary' },
            { value: 'success', label: 'Success' },
            { value: 'info', label: 'Info' },
            { value: 'danger', label: 'Danger' },
            { value: 'warning', label: 'Warning' }
          ]
        }
      }
    ]
  };

  constructor() {}
}

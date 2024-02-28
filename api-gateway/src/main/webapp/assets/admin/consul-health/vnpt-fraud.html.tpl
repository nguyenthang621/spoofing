<ul class="list-unstyled">
  <li><strong>Status:</strong> <span class="float-right badge badge-${ (status === 'UP' ? 'success' : 'danger') }">${ status }</span></li>

  <% if (components.diskSpace) { %>
  <li><strong>Disk Space:</strong> <span class="float-right badge badge-${ (components.diskSpace.status === 'UP' ? 'success' : 'danger')}">${ components.diskSpace.status }</span>
    <ul>
      <li><strong>Total:</strong> <code class="float-right">${ (components.diskSpace.details.total / 1073741824).toFixed(2) + 'GB' }</code></li>
      <li><strong>Free:</strong> <code class="float-right">${ (components.diskSpace.details.free/ 1073741824).toFixed(2) + 'GB' }</code></li>
      <li><strong>Threshold:</strong> <code class="float-right">${ (components.diskSpace.details.threshold/ 1048576).toFixed(2) + 'MB' }</code></li>
    </ul>
  </li>
  <% } %>

  <% if (components.hazelcast) { %>
    <li><strong>Hazelcast:</strong> <span class="float-right badge badge-${ (components.hazelcast.status === 'UP' ? 'success' : 'danger')}">${ components.hazelcast.status }</span>
      <ul>
        <li><strong>Name:</strong> <code class="float-right">${ components.hazelcast.details.name }</code></li>
        <li><strong>UUID:</strong> <code class="float-right">${ components.hazelcast.details.uuid }</code></li>
      </ul>
    </li>
  <% } %>

  <% if (components.mongo) { %>
    <li><strong>mongo:</strong> <span class="float-right badge badge-${ (components.mongo.status === 'UP' ? 'success' : 'danger')}">${ components.mongo.status }</span>
      <ul>
        <li><strong>Version:</strong> <code class="float-right">${ components.mongo.details.version }</code></li>
      </ul>
    </li>
  <% } %>
</ul>

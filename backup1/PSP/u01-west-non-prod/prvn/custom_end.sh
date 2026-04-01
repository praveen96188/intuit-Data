# Listing custom endpoints from old cluster
OLD_ENDPOINTS=$(aws rds --region us-west-2 describe-db-cluster-endpoints \
                --db-cluster-identifier ppsp-ppd-arc \
                --query "DBClusterEndpoints[?EndpointType=='CUSTOM'].DBClusterEndpointIdentifier" \
                --output text)

# Deleting custom endpoints from the old cluster
echo "Deleting custom endpoints from the old cluster..."
for endpoint in $OLD_ENDPOINTS; do
    aws rds  --region us-west-2  delete-db-cluster-endpoint \
        --db-cluster-endpoint-identifier $endpoint
done

# Recreating custom endpoints on the new cluster
echo "Creating custom endpoints on the new cluster..."
for endpoint in $OLD_ENDPOINTS; do
    aws rds create-db-cluster-endpoint \
        --db-cluster-identifier ppsp-ppd-arc \
        --db-cluster-endpoint-identifier $endpoint \
        --endpoint-type CUSTOM \
        --static-members ppsp-ppd-arc1
done

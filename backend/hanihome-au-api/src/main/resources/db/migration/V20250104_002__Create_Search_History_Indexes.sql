-- Search History Performance Indexes
-- Created: 2025-01-04
-- Purpose: Optimize search history queries for better performance

-- Index for user-based queries (most common access pattern)
CREATE INDEX IF NOT EXISTS idx_search_history_user_id 
ON search_history(user_id);

-- Index for user queries ordered by last used date (main listing query)
CREATE INDEX IF NOT EXISTS idx_search_history_user_last_used 
ON search_history(user_id, last_used_at DESC);

-- Index for saved searches queries
CREATE INDEX IF NOT EXISTS idx_search_history_user_saved 
ON search_history(user_id, is_saved, last_used_at DESC);

-- Index for frequent searches (ordered by search count)
CREATE INDEX IF NOT EXISTS idx_search_history_user_search_count 
ON search_history(user_id, is_saved, search_count DESC, last_used_at DESC);

-- Index for cleanup operations (finding old entries)
CREATE INDEX IF NOT EXISTS idx_search_history_cleanup 
ON search_history(user_id, is_saved, created_at);

-- Index for similar search detection
CREATE INDEX IF NOT EXISTS idx_search_history_similar_search 
ON search_history(user_id, keyword, city, min_rent_price, max_rent_price, is_saved);

-- Index for saved search name uniqueness check
CREATE INDEX IF NOT EXISTS idx_search_history_saved_name 
ON search_history(user_id, search_name, is_saved) 
WHERE is_saved = true;

-- Composite index for location-based searches
CREATE INDEX IF NOT EXISTS idx_search_history_location 
ON search_history(user_id, city, state, country, is_saved);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_search_history_date_range 
ON search_history(created_at, updated_at, last_used_at);

-- Add table comment
COMMENT ON TABLE search_history IS 'Stores user search history and saved search conditions with privacy retention policies';

-- Add column comments for documentation
COMMENT ON COLUMN search_history.user_id IS 'References the user who performed the search';
COMMENT ON COLUMN search_history.search_name IS 'Custom name for saved searches, null for regular history';
COMMENT ON COLUMN search_history.is_saved IS 'Indicates if this is a saved search (true) or regular history (false)';
COMMENT ON COLUMN search_history.search_count IS 'Number of times this exact search has been performed';
COMMENT ON COLUMN search_history.last_used_at IS 'Timestamp when this search was last executed';
COMMENT ON COLUMN search_history.created_at IS 'Timestamp when this search was first recorded';
COMMENT ON COLUMN search_history.updated_at IS 'Timestamp when this record was last modified';

-- Performance analysis query (for monitoring)
-- You can use this to monitor index usage:
-- SELECT schemaname, tablename, attname, n_distinct, correlation 
-- FROM pg_stats 
-- WHERE tablename = 'search_history' 
-- ORDER BY n_distinct DESC;
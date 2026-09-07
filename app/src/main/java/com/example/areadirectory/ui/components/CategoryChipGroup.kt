package com.example.areadirectory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Filter mode type for the CategoryChipGroup component.
 */
enum class FilterCategoryType {
    ALL,
    DISTRICTS,
    GOVERNORATES
}

/**
 * Current filter selection state.
 */
data class FilterChipState(
    val selectedType: FilterCategoryType = FilterCategoryType.ALL,
    val selectedItem: String? = null
)

/**
 * Category Chip Group component that enables users to filter search results
 * by 'المحافظات' (Governorates) or 'المديريات' (Districts).
 */
@Composable
fun CategoryChipGroup(
    filterState: FilterChipState,
    districts: List<String>,
    governorates: List<String>,
    onFilterChange: (FilterChipState) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_chip_group"),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Label & Active Filter indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.filter_results_label),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (filterState.selectedType != FilterCategoryType.ALL && filterState.selectedItem != null) {
                    Text(
                        text = filterState.selectedItem,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Primary Category Chips: 'الكل', 'المديريات', 'المحافظات'
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. All
                FilterChip(
                    selected = filterState.selectedType == FilterCategoryType.ALL,
                    onClick = {
                        onFilterChange(FilterChipState(FilterCategoryType.ALL, null))
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.filter_all),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    leadingIcon = {
                        if (filterState.selectedType == FilterCategoryType.ALL) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_chip_all")
                )

                // 2. Filter by Districts
                FilterChip(
                    selected = filterState.selectedType == FilterCategoryType.DISTRICTS,
                    onClick = {
                        if (filterState.selectedType == FilterCategoryType.DISTRICTS) {
                            onFilterChange(FilterChipState(FilterCategoryType.ALL, null))
                        } else {
                            val defaultDistrict = districts.firstOrNull()
                            onFilterChange(FilterChipState(FilterCategoryType.DISTRICTS, defaultDistrict))
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.filter_by_districts),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterState.selectedType == FilterCategoryType.DISTRICTS) Icons.Default.Check else Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("filter_chip_districts")
                )

                // 3. Filter by Governorates
                FilterChip(
                    selected = filterState.selectedType == FilterCategoryType.GOVERNORATES,
                    onClick = {
                        if (filterState.selectedType == FilterCategoryType.GOVERNORATES) {
                            onFilterChange(FilterChipState(FilterCategoryType.ALL, null))
                        } else {
                            val defaultGov = governorates.firstOrNull()
                            onFilterChange(FilterChipState(FilterCategoryType.GOVERNORATES, defaultGov))
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.filter_by_governorates),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterState.selectedType == FilterCategoryType.GOVERNORATES) Icons.Default.Check else Icons.Default.LocationCity,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.testTag("filter_chip_governorates")
                )
            }

            // Secondary Sub-Chips for the selected category (Districts or Governorates)
            AnimatedVisibility(
                visible = filterState.selectedType != FilterCategoryType.ALL,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val currentSubOptions = if (filterState.selectedType == FilterCategoryType.DISTRICTS) {
                    districts
                } else {
                    governorates
                }

                if (currentSubOptions.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentSubOptions.forEach { option ->
                                val isSelected = filterState.selectedItem == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onFilterChange(
                                            FilterChipState(
                                                selectedType = filterState.selectedType,
                                                selectedItem = option
                                            )
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("filter_subchip_$option")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

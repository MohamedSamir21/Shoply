package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes

// Reusable country calling-code picker: an inline display button plus a full-screen searchable
// list shown in a Dialog. State-agnostic (plain lambdas) so any screen can drive it.
@Composable
fun CountryCodePicker(
    modifier: Modifier = Modifier,
    selectedCountryInfo: CountryInfo,
    countries: List<CountryInfo>,
    keyword: String,
    showMenu: Boolean,
    isClickable: Boolean = true,
    bare: Boolean = false,
    onToggleMenu: () -> Unit,
    onKeywordChange: (String) -> Unit,
    onSelect: (CountryInfo) -> Unit,
    onDismiss: () -> Unit
) {
    CountryCodeMenuButton(
        modifier = modifier,
        selectedCountryInfo = selectedCountryInfo,
        isClickable = isClickable,
        bare = bare,
        onClick = onToggleMenu
    )

    if (showMenu) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CountrySearchBar(
                        keyword = keyword,
                        onKeywordChange = onKeywordChange,
                        onCancel = onDismiss
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = countries, key = { it.name }) { country ->
                            CountryRow(country = country, onSelect = onSelect)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryCodeMenuButton(
    modifier: Modifier = Modifier,
    selectedCountryInfo: CountryInfo,
    isClickable: Boolean,
    bare: Boolean,
    onClick: () -> Unit
) {
    // Bare = transparent chip meant to sit inside a text field's leading slot (no
    // border, no fixed height); otherwise a standalone bordered control-height button.
    val containerModifier = if (bare) {
        modifier
            .clip(RoundedCornerShape(ShoplyDimens.space2))
            .clickable { if (isClickable) onClick() }
    } else {
        modifier
            // Same fixed control height as the FormTextField box, so the picker and
            // the phone field are exactly the same height and line up as one row.
            .height(ShoplyDimens.fieldHeight)
            .clip(ShoplyShapes.field)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                ShoplyShapes.field
            )
            .clickable { if (isClickable) onClick() }
            .padding(horizontal = 12.dp)
    }

    Row(
        modifier = containerModifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedCountryInfo.flag,
            textAlign = TextAlign.Center
        )
        Text(
            text = selectedCountryInfo.codeNumber,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.down_arrow),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CountrySearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .padding(10.dp)
                .height(50.dp)
                .weight(1f),
            value = keyword,
            onValueChange = onKeywordChange,
            supportingText = null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            shape = CircleShape
        )
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
private fun CountryRow(
    country: CountryInfo,
    onSelect: (CountryInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(country) }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text(text = country.flag)
            Spacer(Modifier.width(20.dp))
            Text(text = country.name)
        }
        Text(text = country.codeNumber)
    }
}

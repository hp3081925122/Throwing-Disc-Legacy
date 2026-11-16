// Throwing Disc Legacy：CurseForge 创建项目页自动填表脚本（由 build_fill_js.py 生成）
// 用法：作者后台界面切换为英文，打开「Create A Project」页面，按 F12 打开控制台，粘贴本脚本回车
// 脚本逐步填写并自动点 Next、确认头像裁剪框；有字段未填上时停在当前步骤；不会点击最终的创建按钮
(async () => {
  const DATA = {"name": "Throwing Disc Legacy", "summary": "Adds 5 tiers of throwing discs with wall ricochets, direct boomerang return, slot reservation, 6 custom enchantments, and full configuration.", "license": "All Rights Reserved", "dropdowns": [["Class", "Mods"], ["Main category", "Armor, Tools, and Weapons"], ["Additional categories", "Adventure and RPG"], ["Additional categories", "Magic"]], "distribution": "Allow distribution to 3rd party", "allowComments": true, "description": "# Throwing Disc Legacy\n\nA combat and utility weapon mod that recreates the upcoming 2027 Minecraft Throwing Disc weapon, along with dedicated enchantments and expanded mechanics.\n\n## Features\n\n- **5 Disc Tiers**: Craft and upgrade **Copper**, **Iron**, **Golden**, **Diamond**, and **Netherite** throwing discs, each with distinct durability, melee stats, thrown damage, flight speed, range, and bounce count.\n- **Ricochet & Direct Return**: Thrown discs slice through enemies, ricochet off solid blocks, deflect off shields and deflecting entities, and fly straight back to you at a steady speed.\n- **Slot Reservation Placeholder**: Reserving the original main-hand or off-hand slot while the disc is in flight prevents picked-up drops from stealing your throwing hand.\n- **Dispenser Support**: Throwing discs can also be launched directly from dispensers.\n- **Structure Loot Integration**: Configurable loot injection into Trial Chambers, Village Weaponsmiths, Bastion Treasures, End Cities, and Buried Treasures.\n- **Enchantment Descriptions Compatible**: Full localization support for Enchantment Descriptions and similar tooltip mods.\n\n## Custom Enchantments\n\n- **Ricochet**: Increases maximum surface bounces and flight range, while boosting damage with each bounce.\n- **Tailwind**: Increases the flight speed of throwing discs.\n- **Returning Edge**: Deals bonus slicing damage to targets hit while the disc is returning.\n- **Magnetic Recall**: Pulls nearby item drops and XP orbs along the disc's path, and allows sneaking to recall the disc early.\n- **Cleaving Harvest**: Allows thrown or held discs to harvest blocks matching their material tier, carrying thrown block drops and XP back to your hand.\n- **Vein Harvest**: Works with Cleaving Harvest to chain-mine connected ores of the same type and return their drops.\n\n## Configuration\n\nAll disc stats, flight mechanics, enchantment values, loot toggles, slot placeholder behavior, and client trail/spin rendering options can be customized in the mod configs.\n\n**License:** All Rights Reserved\n\n---\n\n# Throwing Disc Legacy（投掷圆盘）\n\n本模组是对 2027 年即将推出的 Minecraft 圆盘武器（Throwing Disc）进行的复刻，并在此基础上加入了专属附魔与配套机制。\n\n## 核心特性\n\n- **5 种材质圆盘**：提供**铜、铁、金、钻石、下界合金** 5 个等级的投掷圆盘，各自拥有独立的耐久、近战属性、投掷伤害、飞行速度、射程与弹射次数。\n- **表面弹射与直线回旋**：圆盘可穿透命中多个目标、在坚硬方块表面连续弹射、在盾牌等目标上触发偏转，并在飞行结束或召回时从当前位置直线匀速飞回手中。\n- **防抢手栏位占位**：投掷后在原主手或副手栏位生成占位物品，防止地面掉落物或带回的战利品占满手持栏位，圆盘返回后自动归位。\n- **发射器支持**：支持放入发射器中作为投射物发射。\n- **结构战利品箱生成**：可在试炼密室、村庄武器匠、堡垒遗迹宝藏、末地城与埋藏宝藏箱中生成（均可在配置中独立开关）。\n- **附魔描述兼容**：内置完整的中英文附魔名称与描述键，兼容 Enchantment Descriptions 等附魔描述模组。\n\n## 专属附魔\n\n- **弹射（Ricochet）**：增加最大表面弹射次数与飞行射程，且每次弹射后提升伤害。\n- **疾旋（Tailwind）**：提升投掷圆盘的飞行速度。\n- **回旋斩（Returning Edge）**：强化圆盘在返程阶段命中目标时造成的切割伤害。\n- **磁吸召回（Magnetic Recall）**：吸附沿途掉落物与经验球并带回，飞行途中潜行可提前召回圆盘。\n- **旋刃采掘（Cleaving Harvest）**：允许投掷或手持圆盘采掘符合材质等级的方块，投掷破坏时会将方块掉落物与经验直接带回手中。\n- **连锁采集（Vein Harvest）**：配合旋刃采掘生效，破坏矿石时自动连锁采掘周围相连的同类矿石并将战利品带回。\n\n## 配置与定制\n\n所有圆盘的基础属性、飞行速度与射程、附魔数值、战利品开关、栏位占位机制以及客户端自转与轨迹粒子效果均可在配置文件中自由调整。\n\n**许可协议：** All Rights Reserved\n", "iconBase64": "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"};
  const report = [];

  // 判断元素是否可见
  const visible = (el) => !!el && el.getClientRects().length > 0 && getComputedStyle(el).visibility !== 'hidden';

  // 收集字段的描述文本（label、aria、placeholder、name、所在容器标题）
  const describe = (el) => {
    const parts = [el.name, el.id, el.placeholder, el.getAttribute('aria-label')];
    if (el.id) {
      const label = document.querySelector(`label[for="${CSS.escape(el.id)}"]`);
      if (label) parts.push(label.textContent);
    }
    const wrapLabel = el.closest('label');
    if (wrapLabel) parts.push(wrapLabel.textContent);
    let node = el.parentElement;
    for (let i = 0; i < 4 && node; i++, node = node.parentElement) {
      const title = node.querySelector('label, h1, h2, h3, h4, legend, .label, [class*="label"], [class*="title"]');
      if (title && !title.contains(el)) {
        parts.push(title.textContent);
        break;
      }
    }
    return parts.filter(Boolean).join(' ').toLowerCase();
  };

  // 按关键词查找字段
  const findField = (keys, excludes, selector) => {
    const list = [...document.querySelectorAll(selector)].filter(visible);
    return list.find((el) => {
      const text = describe(el);
      return keys.some((k) => text.includes(k)) && !excludes.some((k) => text.includes(k));
    });
  };

  // 以 React/Vue 可识别的方式写入输入框
  const setValue = (el, value) => {
    const proto = el.tagName === 'TEXTAREA' ? HTMLTextAreaElement.prototype : el.tagName === 'SELECT' ? HTMLSelectElement.prototype : HTMLInputElement.prototype;
    Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, value);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
    el.dispatchEvent(new Event('blur', { bubbles: true }));
  };

  const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
  const mark = (field, ok, detail) => report.push({ 字段: field, 结果: ok ? '已填写' : '未找到，需手动', 说明: detail || '' });

  // 简易 Markdown 转 HTML（用于富文本编辑器）
  const mdToHtml = (md) => {
    const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const inline = (s) => esc(s)
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
    const lines = md.split(/\r?\n/);
    const out = [];
    let i = 0;
    while (i < lines.length) {
      const line = lines[i];
      if (/^\s*$/.test(line)) { i++; continue; }
      const h = line.match(/^(#{1,6})\s+(.*)$/);
      if (h) { out.push(`<h${h[1].length}>${inline(h[2])}</h${h[1].length}>`); i++; continue; }
      if (/^---+\s*$/.test(line)) { out.push('<hr>'); i++; continue; }
      if (/^>/.test(line)) {
        const quote = [];
        while (i < lines.length && /^>/.test(lines[i])) { quote.push(lines[i].replace(/^>\s?/, '')); i++; }
        out.push('<blockquote>' + quote.filter((q) => q.trim()).map((q) => `<p>${inline(q)}</p>`).join('') + '</blockquote>');
        continue;
      }
      if (/^\|/.test(line)) {
        const rows = [];
        while (i < lines.length && /^\|/.test(lines[i])) { rows.push(lines[i]); i++; }
        const cells = (r) => r.replace(/^\||\|$/g, '').split('|').map((c) => inline(c.trim()));
        const head = cells(rows[0]);
        const body = rows.slice(2).map(cells);
        out.push('<table><thead><tr>' + head.map((c) => `<th>${c}</th>`).join('') + '</tr></thead><tbody>' +
          body.map((r) => '<tr>' + r.map((c) => `<td>${c}</td>`).join('') + '</tr>').join('') + '</tbody></table>');
        continue;
      }
      if (/^\s*[*-]\s+/.test(line)) {
        const items = [];
        while (i < lines.length && /^\s*[*-]\s+/.test(lines[i])) { items.push(lines[i].replace(/^\s*[*-]\s+/, '')); i++; }
        out.push('<ul>' + items.map((t) => `<li>${inline(t)}</li>`).join('') + '</ul>');
        continue;
      }
      const para = [];
      while (i < lines.length && !/^\s*$/.test(lines[i]) && !/^(#{1,6}\s|\||\s*[*-]\s|---|>)/.test(lines[i])) { para.push(lines[i]); i++; }
      out.push(`<p>${inline(para.join(' '))}</p>`);
    }
    return out.join('\n');
  };

  // 6. 下拉框：按标签找到下方最近的下拉触发器，点开后选择选项
  const fireClick = (el) => {
    for (const type of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
      el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }));
    }
  };
  const findLabel = (text) => [...document.querySelectorAll('label, span, div, p, h3, h4, legend')].filter(visible)
    .find((el) => norm(el.textContent) === norm(text) || norm(el.textContent) === norm(text) + ' ?');
  const findTrigger = (label) => {
    const lr = label.getBoundingClientRect();
    const candidates = [...document.querySelectorAll('select, [role="combobox"], [aria-haspopup], button, div, input')].filter(visible)
      .filter((el) => !el.contains(label) && !label.contains(el))
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.bottom - 2 && r.top - lr.bottom < 90 && r.height >= 28 && r.height <= 70 && r.width >= 120 && r.left < lr.left + 60 && r.right > lr.left)
      .sort((a, b) => (a.r.top - b.r.top) || (a.r.width * a.r.height - b.r.width * b.r.height));
    return candidates.length ? candidates[0].el : null;
  };
  const findOption = (text, exclude) => [...document.querySelectorAll('[role="option"], li, label, span, div, button')].filter(visible)
    .filter((el) => !exclude.contains(el) && el.children.length <= 3 && norm(el.textContent) === norm(text))
    .sort((a, b) => a.getBoundingClientRect().height - b.getBoundingClientRect().height)[0];
  // 统一大小写、空白和弯引号，便于匹配 Tinker's 这类选项
  const norm = (t) => (t || '').replace(/[‘’ʼ]/g, "'").replace(/[*\s]+/g, ' ').trim().toLowerCase();
  // 优先找标签下方最近的 [role="combobox"]（2026-10-26 Waystone Horizons 实测可用）
  // 先沿祖先向上找只含一个下拉框的字段容器（License 步骤标签与下拉框之间隔着一段说明文字）
  const findCombo = (label) => {
    let scope = label.parentElement;
    for (let depth = 0; scope && depth < 6; depth++, scope = scope.parentElement) {
      const combos = [...scope.querySelectorAll('[role="combobox"]')].filter(visible);
      if (combos.length === 1) return combos[0];
      if (combos.length > 1) break;
    }
    const lr = label.getBoundingClientRect();
    return [...document.querySelectorAll('[role="combobox"]')].filter(visible)
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.top && r.top - lr.bottom < 260 && r.right > lr.left - 10 && r.left < lr.left + 60)
      .sort((a, b) => a.r.top - b.r.top)[0]?.el || null;
  };
  const comboText = (el) => norm(el.value || el.innerText || el.textContent);
  const waitOption = async (text, timeout = 3000, loose = false) => {
    const end = Date.now() + timeout;
    while (Date.now() < end) {
      const option = [...document.querySelectorAll('[role="option"]')].filter(visible).find((el) => norm(el.innerText || el.textContent) === norm(text) || (loose && norm(el.innerText || el.textContent).includes(norm(text))));
      if (option) return option;
      await sleep(150);
    }
    return null;
  };
  const pickCombo = async (labelText, optionText, combo, loose = false) => {
    const container = combo.closest('[class*="select"], [class*="Select"], [class*="dropdown"], [class*="Dropdown"]') || combo.parentElement;
    if (comboText(combo) === norm(optionText) || (container?.innerText || '').split('\n').some((t) => norm(t) === norm(optionText))) {
      mark(labelText, true, `${optionText}（已选）`);
      return true;
    }
    // MUI Select 只在 mousedown（button=0）时展开，click() 不会触发；Autocomplete 输入框也响应 mousedown
    const press = (el, type, extra = {}) => el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window, button: 0, ...extra }));
    const key = (el, k) => el.dispatchEvent(new KeyboardEvent('keydown', { key: k, code: k, bubbles: true, cancelable: true }));
    combo.focus();
    press(combo, 'mousedown');
    await sleep(400);
    // Autocomplete 只输入撇号前的前缀过滤，避免直引号与弯引号不一致导致过滤为空
    const query = optionText.split(/['’]/)[0].trim();
    if (combo.tagName === 'INPUT' && !combo.readOnly) {
      setValue(combo, query);
      await sleep(500);
    }
    let option = await waitOption(optionText, 3000, loose);
    if (!option) {
      combo.focus();
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option && combo.tagName === 'INPUT') {
      setValue(combo, '');
      press(combo, 'mousedown');
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option) {
      const seen = [...document.querySelectorAll('[role="option"]')].filter(visible).map((el) => (el.innerText || el.textContent).trim()).slice(0, 12);
      mark(labelText, false, `没找到选项 ${optionText}；可见选项：${seen.join(' / ') || '无'}；控件：${combo.tagName}.${String(combo.className).split(' ').slice(0, 3).join('.')}`);
      key(document.activeElement || document.body, 'Escape');
      return true;
    }
    option.scrollIntoView({ block: 'nearest' });
    option.click();
    await sleep(600);
    // 多选菜单点完不会自动收起，用 Esc 关闭，避免挡住后面的下拉框
    const list = option.closest('[role="listbox"]');
    if (list && visible(list)) {
      key(combo, 'Escape');
      key(list, 'Escape');
      await sleep(300);
    }
    mark(labelText, true, optionText);
    return true;
  };
  const pickDropdown = async (labelText, optionText, loose = false) => {
    const label = findLabel(labelText);
    if (!label) { mark(labelText, false, '当前步骤没有该字段'); return; }
    const combo = findCombo(label);
    if (combo && await pickCombo(labelText, optionText, combo, loose)) return;
    const trigger = findTrigger(label);
    if (!trigger) { mark(labelText, false, '未找到下拉框'); return; }
    if (trigger.tagName === 'SELECT') {
      const option = [...trigger.options].find((o) => norm(o.text) === norm(optionText));
      if (option) { setValue(trigger, option.value); mark(labelText, true, optionText); } else mark(labelText, false, `没有选项 ${optionText}`);
      return;
    }
    if (norm(trigger.textContent) === norm(optionText)) { mark(labelText, true, `${optionText}（已选）`); return; }
    fireClick(trigger);
    trigger.focus && trigger.focus();
    await sleep(600);
    const option = findOption(optionText, label);
    if (option) {
      fireClick(option);
      mark(labelText, true, optionText);
      await sleep(800);
      document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
      await sleep(200);
    } else {
      mark(labelText, false, `下拉框已点开但没找到选项 ${optionText}`);
      fireClick(trigger);
    }
  };

  // 按向导步骤决定要填的字段；未知步骤全部尝试
  const STEP_FIELDS = {
    general: ['name', 'summary', 'dropdowns', 'comments', 'icon'],
    description: ['description'],
    license: ['license'],
  };
  const stepOf = () => (location.hash.match(/create\/([a-z-]+)/i) || [])[1] || 'unknown';
  const buttonByText = (re) => [...document.querySelectorAll('button')].filter(visible).find((b) => re.test(norm(b.innerText || b.textContent)) && !b.disabled);

  // 头像裁剪框：等待对话框出现后点确认按钮（图标已是正方形，默认裁剪即整张图）
  const confirmCrop = async () => {
    let dialog = null;
    for (let i = 0; i < 40 && !dialog; i++) {
      dialog = [...document.querySelectorAll('[role="dialog"], .MuiDialog-root, .MuiModal-root')].filter(visible)
        .find((el) => el.querySelector('img, canvas') && !el.closest('.MuiDrawer-root'));
      if (!dialog) await sleep(150);
    }
    if (!dialog) return '没有弹出裁剪框';
    await sleep(500);
    const buttons = [...dialog.querySelectorAll('button')].filter(visible).filter((b) => !b.disabled);
    const ok = buttons.find((b) => /^(save|apply|crop|confirm|done|ok|upload|set|use|submit)\b/.test(norm(b.innerText || b.textContent)));
    if (!ok) return `裁剪框里没找到确认按钮（按钮：${buttons.map((b) => (b.innerText || '').trim()).join(' / ') || '无'}）`;
    ok.click();
    for (let i = 0; i < 40 && dialog.isConnected && visible(dialog); i++) await sleep(150);
    return dialog.isConnected && visible(dialog) ? `已点击 ${ok.innerText.trim()}，但裁剪框没有关闭` : '';
  };

  for (let round = 0; round < 5; round++) {
    report.length = 0;
    const step = stepOf();
    const want = (field) => !STEP_FIELDS[step] || STEP_FIELDS[step].includes(field);
    let nameField = null;
    let alreadyFilled = false;
    let summaryField = null;
    let blocked = '';

    // 1. 项目名称
    if (want('name')) {
      nameField = findField(['project name', 'name'], ['slug', 'summary', 'search', 'user'], 'input[type="text"], input:not([type])');
      alreadyFilled = !!nameField && nameField.value === DATA.name;
      if (nameField) { setValue(nameField, DATA.name); mark('项目名称', true, DATA.name); } else mark('项目名称', false);
    }

    // 2. 简介
    if (want('summary')) {
      summaryField = findField(['summary', 'short description'], ['search'], 'textarea, input[type="text"], input:not([type])');
      if (summaryField) { setValue(summaryField, DATA.summary); mark('简介 Summary', true); } else mark('简介 Summary', false);
    }

    // 3. 下拉框
    if (want('dropdowns')) {
      for (const [labelText, optionText] of DATA.dropdowns) {
        await pickDropdown(labelText, optionText);
      }
    }

    // 4. 允许评论复选框
    if (want('comments') && DATA.allowComments !== false) {
      const box = [...document.querySelectorAll('input[type="checkbox"], [role="checkbox"]')]
        .find((el) => /allow comments/i.test(el.closest('label')?.innerText || el.parentElement?.innerText || el.parentElement?.parentElement?.innerText || el.getAttribute('aria-label') || ''));
      const textNode = [...document.querySelectorAll('label, span, p, div')].filter(visible).find((el) => norm(el.textContent) === 'allow comments');
      const checked = (el) => el && (el.checked === true || el.getAttribute('aria-checked') === 'true');
      if (box || textNode) {
        if (!checked(box)) {
          (box && visible(box) ? box : (box?.closest('label') || textNode)).click();
          await sleep(300);
        }
        mark('Allow Comments', !box || checked(box), box ? (checked(box) ? '已勾选' : '点击后仍未勾选，请手动勾选') : '已点击文字，请确认是否勾上');
      } else mark('Allow Comments', false, '当前步骤没有该选项');
    }

    // 5. 项目图标：写入文件输入框后自动确认裁剪框（名称已是本项目时视为已上传过）
    if (want('icon')) {
      const fileInput = [...document.querySelectorAll('input[type="file"]')].find((f) => !f.accept || /image|png/i.test(f.accept));
      if (fileInput && alreadyFilled) {
        mark('项目图标', true, '已上传过，跳过');
      } else if (fileInput) {
        const bytes = Uint8Array.from(atob(DATA.iconBase64), (c) => c.charCodeAt(0));
        const file = new File([bytes], 'cf-project-icon.png', { type: 'image/png' });
        const dt = new DataTransfer();
        dt.items.add(file);
        fileInput.files = dt.files;
        fileInput.dispatchEvent(new Event('input', { bubbles: true }));
        fileInput.dispatchEvent(new Event('change', { bubbles: true }));
        const cropError = await confirmCrop();
        if (cropError && cropError !== '没有弹出裁剪框') {
          mark('项目图标', false, cropError);
          blocked = '请手动处理头像裁剪框后再运行脚本';
        } else mark('项目图标', true, cropError || '已上传并确认裁剪');
      } else mark('项目图标', false, '请手动上传 curseforge/icon.png');
    }

    // 6. 描述：先切到 Markdown（若页面提供），优先 Markdown 文本框，否则写入富文本编辑器
    if (want('description')) {
      await sleep(800);
      let markdownMode = false;
      const formatSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => /markdown/i.test(o.text)));
      if (formatSelect) {
        const option = [...formatSelect.options].find((o) => /markdown/i.test(o.text));
        setValue(formatSelect, option.value);
        markdownMode = true;
        await sleep(500);
      } else {
        const toggle = [...document.querySelectorAll('button, label, [role="tab"], [role="radio"], input[type="radio"]')].filter(visible)
          .find((el) => /^\s*markdown\s*$/i.test(el.textContent || el.value || ''));
        if (toggle) { toggle.click(); markdownMode = true; await sleep(500); }
      }
      const descField = findField(['description'], ['summary', 'search'], 'textarea');
      const plainTextareas = [...document.querySelectorAll('textarea')].filter(visible).filter((t) => t !== summaryField);
      const targetTextarea = descField || (markdownMode || step === 'description' ? plainTextareas[0] : null);
      if (targetTextarea) {
        setValue(targetTextarea, DATA.description);
        mark('描述', true, 'Markdown 文本框');
      } else {
        let editor = [...document.querySelectorAll('[contenteditable="true"], .ProseMirror, .ql-editor')].filter(visible)[0];
        if (!editor) {
          const frame = [...document.querySelectorAll('iframe')].find((f) => { try { return f.contentDocument && f.contentDocument.body && f.contentDocument.body.isContentEditable; } catch (e) { return false; } });
          if (frame) editor = frame.contentDocument.body;
        }
        if (editor) {
          editor.focus();
          const doc = editor.ownerDocument;
          const sel = doc.getSelection();
          const range = doc.createRange();
          range.selectNodeContents(editor);
          sel.removeAllRanges();
          sel.addRange(range);
          const html = mdToHtml(DATA.description);
          if (!doc.execCommand('insertHTML', false, html)) editor.innerHTML = html;
          editor.dispatchEvent(new Event('input', { bubbles: true }));
          mark('描述', true, '富文本编辑器（请检查表格排版）');
        } else mark('描述', false, '没找到描述编辑器，请手动粘贴 description.md');
      }
    }

    // 7. 许可证：原生下拉框或 MUI 下拉框，选项文本包含目标即可（如 MIT License）
    if (want('license') && DATA.license) {
      const licenseText = DATA.license.toLowerCase();
      const licenseSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => o.text.toLowerCase().includes(licenseText)));
      if (licenseSelect) {
        const option = [...licenseSelect.options].find((o) => o.text.toLowerCase().includes(licenseText));
        setValue(licenseSelect, option.value);
        mark('许可证', true, option.text);
      } else {
        await pickDropdown(findLabel('Project License') ? 'Project License' : 'License', DATA.license, true);
      }
    }

    // 8. 第三方分发：默认允许
    if (want('license') && DATA.distribution) {
      await pickDropdown('Project distribution', DATA.distribution);
    }

    console.log(`%c步骤 ${step}`, 'font-weight:bold');
    console.table(report);
    const failed = report.filter((r) => r.结果 !== '已填写');
    if (blocked || failed.length) {
      console.log(`%c${blocked || '有字段未填上，已停在当前步骤；处理后可再次运行脚本'}`, 'color:#c92a2a;font-weight:bold');
      break;
    }
    // 9. 只点 Next，不点创建按钮；进入下一步后继续填写
    const next = buttonByText(/^next$/);
    if (!next) {
      console.log('%c已到最后一步：请核对全部内容，确认无误后手动点击创建按钮。', 'color:#2b8a3e;font-weight:bold');
      break;
    }
    const before = location.hash;
    await sleep(400);
    next.click();
    let moved = false;
    for (let i = 0; i < 60 && !moved; i++) { await sleep(150); moved = location.hash !== before; }
    if (!moved) {
      console.log('%c点击 Next 后页面没有前进，可能有必填项未通过校验，请查看页面提示。', 'color:#c92a2a;font-weight:bold');
      break;
    }
    await sleep(1200);
  }
})();
